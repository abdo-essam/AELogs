package com.ae.logs.core.bus

/**
 * Marker interface for all AELogs events flowing through the [EventBus].
 *
 * Define custom events by implementing this interface:
 * ```kotlin
 * data class MyPluginEvent(val data: String) : AELogsEvent
 * ```
 *
 * Built-in events are provided for common cross-plugin signals.
 */
public interface AELogsEvent

/** Fired by AELogs when the panel UI is opened. */
public object PanelOpenedEvent : AELogsEvent

/** Fired by AELogs when the panel UI is closed. */
public object PanelClosedEvent : AELogsEvent

/** Fired by AELogs when the host app comes to the foreground. */
public object AppStartedEvent : AELogsEvent

/** Fired by AELogs when the host app goes to the background. */
public object AppStoppedEvent : AELogsEvent

/** Fired after [com.ae.logs.AELogs.clearAll] — signals all plugins to reset their state. */
public object AllDataClearedEvent : AELogsEvent

/** Fired by plugins to register their custom tags with the logs viewer dynamically. */
public data class RegisterLogTagEvent(
    val tag: String,
    val badgeLabel: String,
) : AELogsEvent
