package com.ae.logs.core

import com.ae.logs.AELogsConfig
import com.ae.logs.core.bus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * Scoped context passed to each plugin on [AELogsPlugin.onAttach].
 *
 * Provides a controlled, minimal API surface — plugins only get what they need,
 * not a reference to the full [com.ae.logs.AELogs] instance.
 *
 * ## What plugins CAN do via context
 * - Launch coroutines safely via [scope] (auto-cancelled before [AELogsPlugin.onDetach])
 * - Read global config via [config]
 * - Publish / subscribe to events via [eventBus]
 * - Look up sibling plugins via [getPlugin]
 *
 * ## What plugins CANNOT do
 * - Install or uninstall other plugins
 * - Show / hide the AELogs overlay
 * - Access internal lifecycle machinery
 */
public interface PluginContext {
    /**
     * CoroutineScope tied to this plugin's lifetime.
     *
     * All coroutines launched here are cancelled automatically when the plugin is
     * detached — no manual cleanup required. Uses [kotlinx.coroutines.SupervisorJob]
     * so one failing child doesn't cancel sibling coroutines.
     */
    public val scope: CoroutineScope

    /** Read-only view of the global AELogs configuration. */
    public val config: AELogsConfig

    /**
     * Shared event bus for cross-plugin communication.
     *
     * Publish custom events or subscribe to events from other plugins.
     * Built-in system events ([com.ae.logs.core.bus.PanelOpenedEvent], etc.)
     * are published automatically by AELogs itself.
     */
    public val eventBus: EventBus

    /**
     * Look up a sibling plugin by type.
     *
     * Returns `null` if the plugin is not installed.
     *
     * ```kotlin
     * val logs = context.getPlugin<LogsPlugin>()
     * ```
     */
    public fun <T : AELogsPlugin> getPlugin(type: KClass<T>): T?
}

/**
 * Kotlin reified convenience wrapper for [PluginContext.getPlugin].
 *
 * ```kotlin
 * val logs = context.getPlugin<LogsPlugin>()
 * ```
 */
public inline fun <reified T : AELogsPlugin> PluginContext.getPlugin(): T? = getPlugin(T::class)
