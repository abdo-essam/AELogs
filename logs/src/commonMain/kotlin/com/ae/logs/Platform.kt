package com.ae.logs

/** Platform-specific utilities. */
internal expect class Platform() {
    val name: String
}
