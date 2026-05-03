package com.ae.log.plugins.log

import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

/**
 * A pre-tagged logger that eliminates tag repetition on every call.
 *
 * Create one per class via [AELog.logger]:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELog.logger("AuthViewModel")
 *
 *     fun login() {
 *         log.d("Login started")           // tag is baked in
 *         log.e("Token refresh failed", throwable)
 *     }
 * }
 * ```
 *
 * All methods are **silent no-ops** if [AELog.init] has not been called yet.
 *
 * @param tag The tag that will be prepended to every log entry.
 */
public class Logger(
    public val tag: String,
) {
    /** Log a [LogSeverity.VERBOSE] message. */
    public fun v(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.VERBOSE, tag, message, throwable)
    }

    /** Log a [LogSeverity.DEBUG] message. */
    public fun d(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.DEBUG, tag, message, throwable)
    }

    /** Log a [LogSeverity.INFO] message. */
    public fun i(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.INFO, tag, message, throwable)
    }

    /** Log a [LogSeverity.WARN] message. */
    public fun w(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.WARN, tag, message, throwable)
    }

    /** Log a [LogSeverity.ERROR] message. */
    public fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.ERROR, tag, message, throwable)
    }

    /** Log a [LogSeverity.ASSERT] ("What a Terrible Failure") message. */
    public fun wtf(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.log?.log(LogSeverity.ASSERT, tag, message, throwable)
    }
}

// ── Factory ───────────────────────────────────────────────────────────────────

/**
 * Creates a [Logger] pre-bound to [tag].
 *
 * ```kotlin
 * // At class level — tag is set once, used everywhere
 * private val log = AELog.logger("AuthViewModel")
 *
 * log.d("Login started")
 * log.e("Failed", throwable)
 * ```
 */
public fun AELog.Companion.logger(tag: String): Logger = Logger(tag)
