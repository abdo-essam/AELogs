package com.ae.logs.core.utils

import kotlin.uuid.Uuid

/**
 * Centralized ID generation for AELogs.
 */
public object IdGenerator {
    /**
     * Generates a unique, collision-resistant string ID.
     */
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    public fun generateId(): String = Uuid.random().toString()
}
