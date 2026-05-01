package com.ae.logs.plugins.network.model

/** Filter options for the network panel. */
public enum class NetworkFilter(
    public val label: String,
) {
    ALL("All"),
    PENDING("Pending"),
    SUCCESS("2xx"),
    ERRORS("Errors"),
}
