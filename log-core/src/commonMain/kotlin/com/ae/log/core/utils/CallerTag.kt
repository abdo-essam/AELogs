package com.ae.log.core.utils

/**
 * Returns a short tag derived from the immediate caller outside AELog internals.
 *
 * Used when no explicit tag is supplied — mirrors Android's `Log` behavior of
 * using the simple class name as the tag.
 *
 * This is an `internal` detail; callers never see it.
 */
internal expect fun callerTag(): String
