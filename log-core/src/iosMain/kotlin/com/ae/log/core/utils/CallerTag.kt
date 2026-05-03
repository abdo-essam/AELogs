package com.ae.log.core.utils

import platform.Foundation.NSThread

/**
 * iOS actual for callerTag() — reads the call-stack symbols from [NSThread]
 * and extracts the first frame outside AELog internals.
 *
 * Falls back to "AELog" if the symbols are unavailable (Release / bitcode).
 */
internal actual fun callerTag(): String {
    val skipSubstrings = listOf("com.ae.log", "AELog")
    val symbols = NSThread.callStackSymbols
    val frame =
        symbols
            .drop(1) // skip callerTag itself
            .firstOrNull { sym ->
                val s = sym as? String ?: return@firstOrNull false
                skipSubstrings.none { s.contains(it) }
            } as? String

    // Symbol format: "3  MyApp  0x00000001 MyClass.myFunc + 12"
    return frame
        ?.split(Regex("\\s+"))
        ?.getOrNull(3) // 4th whitespace-separated token is the mangled symbol
        ?.substringBefore('.') // take everything before the first dot
        ?.ifBlank { null }
        ?: "AELog"
}
