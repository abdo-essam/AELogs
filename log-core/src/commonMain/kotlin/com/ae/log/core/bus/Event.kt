package com.ae.log.core.bus

/**
 * Marker interface for all AELog events flowing through the [EventBus].
 *
 * Define custom events by implementing this interface:
 * ```kotlin
 * data class MyPluginEvent(val data: String) : Event
 * ```
 *
 * Built-in events are provided for common cross-plugin signals.
 */
public interface Event

/** Fired by AELog when the panel UI is opened. */
public object PanelOpenedEvent : Event

/** Fired by AELog when the panel UI is closed. */
public object PanelClosedEvent : Event

/** Fired by AELog when the host app comes to the foreground. */
public object AppStartedEvent : Event

/** Fired by AELog when the host app goes to the background. */
public object AppStoppedEvent : Event

/** Fired after [com.ae.log.AELog.clearAll] — signals all plugins to reset their state. */
public object AllDataClearedEvent : Event

/** Fired by plugins to indicate that a log tag should be treated as an analytics event. */
public data class LogTagRegisteredEvent(
    val tag: String,
    val badgeLabel: String,
) : Event
