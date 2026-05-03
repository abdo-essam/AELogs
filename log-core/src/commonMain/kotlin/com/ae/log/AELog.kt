package com.ae.log

import com.ae.log.core.Plugin
import com.ae.log.core.bus.EventBus
import com.ae.log.core.utils.callerTag
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.log.model.LogSeverity
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic

/**
 * AELog — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * This is the primary entry point for the SDK.
 *
 * ## 1. Setup
 * ```kotlin
 * AELog.init(LogPlugin(), NetworkPlugin())
 * ```
 *
 * ## 2. Logging
 * ```kotlin
 * AELog.d("MyTag", "Hello World") // Explicit tag
 * AELog.d("Hello World")         // Auto-tag (derived from class name)
 * ```
 */
public object AELog {
    private val _instance = atomic<LogInspector?>(null)

    /**
     * Internal accessor for the default inspector instance.
     * Hidden from public autocomplete to keep the API clean.
     */
    @PublishedApi
    internal val instance: LogInspector? get() = _instance.value

    /**
     * Initialise the shared AELog instance.
     *
     * @param plugins Plugins to install (LogPlugin, NetworkPlugin, etc.)
     * @param config Global configuration.
     */
    @JvmStatic
    public fun init(
        vararg plugins: Plugin,
        config: LogConfig = LogConfig(),
    ) {
        _instance.value?.let { return }
        val newInstance = LogInspector(config)
        if (_instance.compareAndSet(null, newInstance)) {
            plugins.forEach { newInstance.plugins.install(it) }
        }
    }

    private val _isEnabled = atomic(true)

    /** Global toggle to enable/disable all AELog activity. */
    @JvmStatic
    public var isEnabled: Boolean
        get() = _isEnabled.value
        set(value) { _isEnabled.value = value }

    /** Look up an installed plugin by type on the shared instance. */
    public inline fun <reified T : Plugin> getPlugin(): T? =
        instance?.plugins?.getPlugin(T::class)

    /**
     * Generic log entry point — useful for bridges and integrations.
     * For standard app logging, prefer the shorthands like [d] or [e].
     */
    @JvmStatic
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        record(severity, tag, message, throwable)
    }

    /** Export all recorded data from all plugins as a string. */
    @JvmStatic
    public fun export(): String = instance?.export() ?: ""

    /** Clear all data from all plugins. */
    @JvmStatic
    public fun clearAll(): Unit = instance?.clearAll() ?: Unit

    // ── Logging Methods ──────────────────────────────────────────────────────

    @JvmStatic
    public fun v(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.VERBOSE, tag, msg, t)
    @JvmStatic
    public fun d(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.DEBUG, tag, msg, t)
    @JvmStatic
    public fun i(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.INFO, tag, msg, t)
    @JvmStatic
    public fun w(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.WARN, tag, msg, t)
    @JvmStatic
    public fun e(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.ERROR, tag, msg, t)
    @JvmStatic
    public fun wtf(tag: String, msg: String, t: Throwable? = null): Unit = record(LogSeverity.ASSERT, tag, msg, t)

    @JvmStatic
    public fun v(msg: String, t: Throwable? = null): Unit = record(LogSeverity.VERBOSE, callerTag(), msg, t)
    @JvmStatic
    public fun d(msg: String, t: Throwable? = null): Unit = record(LogSeverity.DEBUG, callerTag(), msg, t)
    @JvmStatic
    public fun i(msg: String, t: Throwable? = null): Unit = record(LogSeverity.INFO, callerTag(), msg, t)
    @JvmStatic
    public fun w(msg: String, t: Throwable? = null): Unit = record(LogSeverity.WARN, callerTag(), msg, t)
    @JvmStatic
    public fun e(msg: String, t: Throwable? = null): Unit = record(LogSeverity.ERROR, callerTag(), msg, t)
    @JvmStatic
    public fun wtf(msg: String, t: Throwable? = null): Unit = record(LogSeverity.ASSERT, callerTag(), msg, t)

    @PublishedApi
    internal fun record(severity: LogSeverity, tag: String, msg: String, t: Throwable?) {
        instance?.record(severity, tag, msg, t)
    }
}

/**
 * The actual engine behind AELog. Holds plugins, event bus, and state.
 *
 * All properties are internal to keep the public API surface area minimal.
 */
public class LogInspector internal constructor(
    internal val config: LogConfig,
) {
    internal val eventBus: EventBus = EventBus()
    @PublishedApi
    internal val plugins: PluginManager = PluginManager(config, eventBus)
    internal val lifecycle: Lifecycle = Lifecycle(plugins, eventBus)

    internal fun record(severity: LogSeverity, tag: String, msg: String, t: Throwable?) {
        plugins.getPlugin(LogPlugin::class)?.recorder?.log(severity, tag, msg, t)
    }

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { plugin ->
            val data = plugin.export()
            if (data.isNotBlank()) {
                sb.append("--- ${plugin.name} ---\n$data\n\n")
            }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}
