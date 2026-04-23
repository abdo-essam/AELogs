package com.ae.devlens.core.utils

import kotlin.uuid.Uuid

/**
 * Centralized ID generation for AEDevLens.
 */
public object IdGenerator {
    /**
     * Generates a unique, collision-resistant string ID.
     */
    public fun generateId(): String {
        return Uuid.random().toString()
    }
}
