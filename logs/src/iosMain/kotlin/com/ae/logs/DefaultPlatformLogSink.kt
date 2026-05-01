package com.ae.logs

import com.ae.logs.plugins.logs.model.LogSeverity

internal actual class DefaultPlatformLogSink : PlatformLogSink {
    actual override fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val formattedMsg = "[$severity] $tag: $message"
        if (throwable != null) {
            println("$formattedMsg\n${throwable.stackTraceToString()}")
        } else {
            println(formattedMsg)
        }
    }
}
