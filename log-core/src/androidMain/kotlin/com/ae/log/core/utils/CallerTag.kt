package com.ae.log.core.utils

/**
 * Walks the JVM/Android stack to find the first frame outside the AELog
 * library packages and returns its simple class name as the tag.
 *
 * Falls back to "AELog" if nothing useful is found (e.g., in obfuscated builds).
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
        ?.substringBefore('$')   // strip anonymous/inner class suffixes
        ?: "AELog"
}
