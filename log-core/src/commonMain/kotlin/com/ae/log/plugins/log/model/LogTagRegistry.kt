package com.ae.log.plugins.log.model

import kotlinx.atomicfu.update

/**
 * Registry for log tags that should be treated as Analytics events.
 *
 * This registry is now instance-based (owned by [com.ae.log.plugins.log.LogPlugin])
 * to support multi-instance AELog setups without leaking state between them.
 */
public class LogTagRegistry {
    private val tags = kotlinx.atomicfu.atomic(emptyMap<String, String>())

    public fun register(
        tag: String,
        label: String,
    ) {
        tags.update { it + (tag to label) }
    }

    public fun isRegistered(tag: String): Boolean = tags.value.containsKey(tag)

    public fun getLabel(tag: String): String? = tags.value[tag]
}
