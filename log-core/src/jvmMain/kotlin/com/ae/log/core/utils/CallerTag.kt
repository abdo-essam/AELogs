package com.ae.log.core.utils

/**
 * JVM actual for callerTag() — same stack-walk logic as Android.
 */
internal actual fun callerTag(): String {
    val skipPrefixes = listOf("com.ae.log", "java.", "kotlin.")
    return Throwable()
        .stackTrace
        .firstOrNull { frame ->
            skipPrefixes.none { frame.className.startsWith(it) }
        }
        ?.className
        ?.substringAfterLast('.')
        ?.substringBefore('$')
        ?: "AELog"
}
