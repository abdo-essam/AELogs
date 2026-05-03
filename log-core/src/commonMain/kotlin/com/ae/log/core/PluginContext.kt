package com.ae.log.core

import com.ae.log.LogConfig
import com.ae.log.core.bus.EventBus
import com.ae.log.core.bus.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Scoped context passed to each plugin on [Plugin.onAttach].
 *
 * Provides a controlled, minimal API surface — plugins only get what they need,
 * not a reference to the full [com.ae.log.AELog] instance.
 *
 * ## What plugins CAN do via context
 * - Launch coroutines safely via [scope] (auto-cancelled before [Plugin.onDetach])
 * - Read global config via [config]
 * - Publish / subscribe to events via [eventBus]
 * - Look up sibling plugins via [getPlugin]
 *
 * ## What plugins CANNOT do
 * - Install or uninstall other plugins
 * - Show / hide the AELog overlay
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

    /** Read-only view of the global AELog configuration. */
    public val config: LogConfig

    /**
     * Shared event bus for cross-plugin communication.
     *
     * Publish custom events or subscribe to events from other plugins.
     * Built-in system events ([com.ae.log.core.bus.PanelOpenedEvent], etc.)
     * are published automatically by AELog itself.
     */
    public val eventBus: EventBus

    /**
     * Look up a sibling plugin by type.
     *
     * Returns `null` if the plugin is not installed.
     *
     * ```kotlin
     * val logs = context.getPlugin<LogPlugin>()
     * ```
     */
    public fun <T : Plugin> getPlugin(type: KClass<T>): T?
}

/**
 * Kotlin reified convenience wrapper for [PluginContext.getPlugin].
 */
public inline fun <reified T : Plugin> PluginContext.getPlugin(): T? = getPlugin(T::class)

/**
 * Convenience helper to collect events of type [T] from the [EventBus].
 *
 * ```kotlin
 * override fun onAttach(context: PluginContext) {
 *     context.collectEvents<MyEvent> { event ->
 *         // handle event
 *     }
 * }
 * ```
 */
public inline fun <reified T : com.ae.log.core.bus.Event> PluginContext.collectEvents(
    crossinline action: suspend (T) -> Unit,
): kotlinx.coroutines.Job =
    scope.launch {
        eventBus.subscribe<T>().collect { event ->
            action(event)
        }
    }
