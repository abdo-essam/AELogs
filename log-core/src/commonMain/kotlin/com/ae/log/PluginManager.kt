package com.ae.log

import com.ae.log.core.Plugin
import com.ae.log.core.PluginContext
import com.ae.log.core.bus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.reflect.KClass
import kotlinx.atomicfu.getAndUpdate as atomicGetAndUpdate
import kotlinx.atomicfu.update as atomicUpdate

/**
 * Manages the full lifecycle of registered [Plugin]s.
 *
 * Owns per-plugin [CoroutineScope]s (via [SupervisorJob]) so they are
 * automatically cancelled when the plugin is detached — callers never
 * need to manage scopes manually.
 *
 * This class is internal to `logs-core`. Consumers interact with plugins
 * through [AELog], which delegates here.
 *
 * ## Responsibilities
 * - Install / uninstall plugins (de-duplicated by ID)
 * - Build and pass [PluginContext] on attach
 * - Cancel plugin scope before calling [Plugin.onDetach]
 * - Provide type-safe and ID-based plugin lookup
 */
public class PluginManager internal constructor(
    private val config: LogConfig,
    private val eventBus: EventBus,
) {
    private val _plugins = MutableStateFlow<List<Plugin>>(emptyList())

    /** Hot stream of all currently registered plugins. */
    public val plugins: StateFlow<List<Plugin>> = _plugins.asStateFlow()

    /** Per-plugin coroutine scopes, keyed by plugin id. */
    private val scopes = kotlinx.atomicfu.atomic(emptyMap<String, CoroutineScope>())

    // ── Registration ──────────────────────────────────────────────────────────

    public fun install(plugin: Plugin): PluginManager {
        val wasAdded =
            _plugins
                .updateAndGet { current ->
                    if (current.any { it.id == plugin.id }) current else current + plugin
                }.contains(plugin)

        if (wasAdded && !scopes.value.containsKey(plugin.id)) {
            val scope = CoroutineScope(SupervisorJob() + config.dispatcher)
            scopes.atomicUpdate { it + (plugin.id to scope) }
            safeCall { plugin.onAttach(buildContext(scope)) }
        }
        return this
    }

    public fun uninstall(pluginId: String): PluginManager {
        var detached: Plugin? = null
        _plugins.update { current ->
            val plugin = current.find { it.id == pluginId } ?: return@update current
            detached = plugin
            current.filter { it.id != pluginId }
        }
        detached?.let { plugin ->
            val removedScope = scopes.atomicGetAndUpdate { it - plugin.id }[plugin.id]
            removedScope?.cancel()
            safeCall { plugin.onDetach() }
        }
        return this
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    public fun <T : Plugin> getPlugin(type: KClass<T>): T? = _plugins.value.firstOrNull { type.isInstance(it) } as? T

    public fun getPluginById(id: String): Plugin? = _plugins.value.find { it.id == id }

    // ── Batch iteration ───────────────────────────────────────────────────────

    /** Iterate all plugins safely — one failure doesn't stop the rest. */
    internal fun forEach(action: (Plugin) -> Unit) = _plugins.value.forEach { safeCall { action(it) } }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun buildContext(scope: CoroutineScope): PluginContext =
        object : PluginContext {
            override val scope: CoroutineScope = scope
            override val config: LogConfig = this@PluginManager.config
            override val eventBus: EventBus = this@PluginManager.eventBus

            @Suppress("UNCHECKED_CAST")
            override fun <T : Plugin> getPlugin(type: KClass<T>): T? = this@PluginManager.getPlugin(type)
        }

    /** Runs [block] and swallows exceptions so one bad plugin can't crash others. */
    private fun safeCall(block: () -> Unit) {
        runCatching { block() }.onFailure { error ->
            config.errorHandler.invoke(error)
        }
    }
}
