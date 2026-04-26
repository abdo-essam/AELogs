package com.ae.logs

import com.ae.logs.core.AELogsPlugin
import com.ae.logs.core.bus.AllDataClearedEvent
import com.ae.logs.core.bus.AppStartedEvent
import com.ae.logs.core.bus.AppStoppedEvent
import com.ae.logs.core.bus.EventBus
import com.ae.logs.core.bus.PanelClosedEvent
import com.ae.logs.core.bus.PanelOpenedEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * AELogs — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * The main entry point to the SDK. Coordinates three sub-systems:
 *
 * ```
 * AELogs
 * ├── PluginManager  — registration, lifecycle & scope management
 * ├── EventBus       — cross-plugin pub/sub
 * └── AELogsConfig  — global configuration
 * ```
 *
 * ## Setup (fluent builder style)
 * ```kotlin
 * val inspector = AELogs.create(AELogsConfig())
 *     .install(LogsPlugin(maxEntries = 1000))
 *     .install(NetworkPlugin())
 *     .install(CrashPlugin())
 * ```
 *
 * ## Simple setup (default instance + auto-install)
 * ```kotlin
 * val inspector = AELogs.createDefault()   // from :logs aggregator
 * ```
 *
 * ## App lifecycle integration
 * ```kotlin
 * inspector.notifyStart()   // call from onStart()
 * inspector.notifyStop()    // call from onStop()
 * ```
 */
public class AELogs private constructor(
    config: AELogsConfig,
) {
    // ── Sub-systems ───────────────────────────────────────────────────────────

    /**
     * Shared event bus for all plugins on this instance.
     * Also exposed to each plugin via [com.ae.logs.core.PluginContext.eventBus].
     */
    public val eventBus: EventBus = EventBus()

    /** Internal plugin lifecycle manager. */
    private val pluginManager = PluginManager(config, eventBus)

    /** Hot stream of all currently registered plugins. */
    public val plugins: StateFlow<List<AELogsPlugin>> = pluginManager.plugins

    // ── Fluent plugin registration ────────────────────────────────────────────

    /**
     * Install a plugin and return **this** instance for chaining.
     *
     * Duplicate plugin IDs are silently ignored (idempotent).
     * [com.ae.logs.core.AELogsPlugin.onAttach] is called synchronously.
     *
     * ```kotlin
     * AELogs.create()
     *     .install(LogsPlugin())
     *     .install(NetworkPlugin())
     *     .install(CrashPlugin())
     * ```
     */
    public fun install(plugin: AELogsPlugin): AELogs {
        pluginManager.install(plugin)
        return this
    }

    /**
     * Uninstall a plugin by ID and return **this** instance for chaining.
     *
     * The plugin's [com.ae.logs.core.PluginContext.scope] is cancelled
     * before [com.ae.logs.core.AELogsPlugin.onDetach] is called.
     */
    public fun uninstall(pluginId: String): AELogs {
        pluginManager.uninstall(pluginId)
        return this
    }

    // ── Plugin lookup ─────────────────────────────────────────────────────────

    /**
     * Get a registered plugin by type. Returns `null` if not installed.
     *
     * ```kotlin
     * val logs: LogsPlugin? = inspector.getPlugin<LogsPlugin>()
     * ```
     */
    public inline fun <reified T : AELogsPlugin> getPlugin(): T? = plugins.value.filterIsInstance<T>().firstOrNull()

    /** Get a registered plugin by its stable string ID. */
    public fun getPluginById(id: String): AELogsPlugin? = pluginManager.getPluginById(id)

    // ── Lifecycle notifications ───────────────────────────────────────────────

    /**
     * Notify all plugins the host app has moved to the **foreground**.
     * Publishes [AppStartedEvent] to [eventBus].
     */
    public fun notifyStart() {
        pluginManager.forEach { it.onStart() }
        eventBus.publish(AppStartedEvent)
    }

    /**
     * Notify all plugins the host app has moved to the **background**.
     * Publishes [AppStoppedEvent] to [eventBus].
     */
    public fun notifyStop() {
        pluginManager.forEach { it.onStop() }
        eventBus.publish(AppStoppedEvent)
    }

    /**
     * Notify all plugins the AELogs UI panel has been **opened**.
     * Publishes [PanelOpenedEvent] to [eventBus].
     */
    public fun notifyOpen() {
        pluginManager.forEach { it.onOpen() }
        eventBus.publish(PanelOpenedEvent)
    }

    /**
     * Notify all plugins the AELogs UI panel has been **closed**.
     * Publishes [PanelClosedEvent] to [eventBus].
     */
    public fun notifyClose() {
        pluginManager.forEach { it.onClose() }
        eventBus.publish(PanelClosedEvent)
    }

    /** Clear all plugin data and publish [AllDataClearedEvent]. */
    public fun clearAll() {
        pluginManager.forEach { it.onClear() }
        eventBus.publish(AllDataClearedEvent)
    }

    // ── Companion (factory) ───────────────────────────────────────────────────

    public companion object {
        /**
         * Shared default instance for apps that only need one inspector.
         *
         * Call [install] on this instance on app startup, or use
         * `AELogs.createDefault()` from the `:logs` aggregator for
         * a pre-configured instance with [LogsPlugin] included.
         */
        public val default: AELogs by lazy { create() }

        /**
         * Create a new isolated [AELogs] instance with custom configuration.
         *
         * Prefer the fluent style:
         * ```kotlin
         * val inspector = AELogs.create(AELogsConfig())
         *     .install(LogsPlugin(maxEntries = 1000))
         * ```
         */
        public fun create(config: AELogsConfig = AELogsConfig()): AELogs = AELogs(config)
    }
}
