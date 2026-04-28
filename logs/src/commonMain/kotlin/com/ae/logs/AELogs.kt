package com.ae.logs

import com.ae.logs.core.AELogsPlugin
import com.ae.logs.core.bus.AllDataClearedEvent
import com.ae.logs.core.bus.AppStartedEvent
import com.ae.logs.core.bus.AppStoppedEvent
import com.ae.logs.core.bus.EventBus
import com.ae.logs.core.bus.PanelClosedEvent
import com.ae.logs.core.bus.PanelOpenedEvent
import kotlinx.atomicfu.atomic
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
 * ## Setup — single entry point
 * ```kotlin
 * // In Application.onCreate()
 * AELogs.init(LogsPlugin(), NetworkPlugin(), AnalyticsPlugin())
 * ```
 *
 * ## Zero-config
 * ```kotlin
 * AELogs.init()
 * ```
 *
 * ## Accessing plugin APIs after init
 * ```kotlin
 * val networkApi = AELogs.plugin<NetworkPlugin>()?.api
 * ```
 *
 * ## App lifecycle integration
 * ```kotlin
 * AELogs.default.notifyStart()   // call from onStart()
 * AELogs.default.notifyStop()    // call from onStop()
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

    // ── Companion (factory & singleton) ──────────────────────────────────────

    public companion object {
        private val _default = atomic<AELogs?>(null)

        /**
         * The shared default [AELogs] instance.
         *
         * Requires [init] to have been called first — throws [IllegalStateException]
         * with a clear message if accessed before initialisation.
         *
         * ```kotlin
         * // Always call init first:
         * AELogs.init(LogsPlugin())
         *
         * // Then access the instance anywhere:
         * val instance = AELogs.default
         * ```
         */
        public val default: AELogs
            get() =
                _default.value ?: error(
                    "AELogs has not been initialised. " +
                        "Call AELogs.init() in Application.onCreate() before accessing AELogs.default.",
                )

        /**
         * Null-safe internal accessor used by log extensions so they silently
         * no-op if [init] has not been called yet — consistent with how
         * Timber and similar libraries behave before a tree is planted.
         */
        @PublishedApi
        internal fun defaultOrNull(): AELogs? = _default.value

        /**
         * Initialise AELogs and configure the shared [default] instance.
         *
         * **Idempotent** — safe to call multiple times; only the first call
         * creates and configures the instance. Subsequent calls return the
         * already-initialised [default] immediately.
         *
         * ```kotlin
         * // Zero-config
         * AELogs.init()
         *
         * // With plugins
         * AELogs.init(LogsPlugin(), NetworkPlugin(), AnalyticsPlugin())
         *
         * // With custom config
         * AELogs.init(LogsPlugin(), config = AELogsConfig())
         * ```
         *
         * @param plugins  Plugins to install on the shared instance.
         * @param config   Core configuration (only applied on first call).
         * @return The shared [default] instance.
         */
        public fun init(
            vararg plugins: AELogsPlugin,
            config: AELogsConfig = AELogsConfig(),
        ): AELogs {
            // Fast path: already initialised
            _default.value?.let { return it }

            val instance = AELogs(config)
            plugins.forEach { instance.install(it) }

            // CAS guarantees only one winner on concurrent calls; the loser
            // discards its instance and returns the already-set singleton.
            return if (_default.compareAndSet(null, instance)) instance else _default.value!!
        }

        /**
         * Look up a plugin on the [default] instance by type.
         *
         * Returns `null` if [init] has not been called or if the plugin
         * is not installed — never throws.
         *
         * ```kotlin
         * val networkApi = AELogs.plugin<NetworkPlugin>()?.api
         * ```
         */
        public inline fun <reified T : AELogsPlugin> plugin(): T? = defaultOrNull()?.getPlugin<T>()

        /**
         * Create a new **isolated** [AELogs] instance with custom configuration.
         *
         * Use for advanced scenarios (e.g. tests, embedded SDKs) where a
         * separate instance is required. For the common case, prefer [init]
         * which configures the shared singleton.
         *
         * ```kotlin
         * val testInstance = AELogs.create()
         *     .install(LogsPlugin())
         * ```
         */
        public fun create(config: AELogsConfig = AELogsConfig()): AELogs = AELogs(config)
    }
}
