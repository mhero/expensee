package com.mac.expensee.core.network.config

/** Runtime-visible network configuration, sourced from BuildConfig at construction time. */
data class ApiConfig(val baseUrl: String) {

    /**
     * There is no backend yet -- `BuildConfig.API_BASE_URL` always points at this placeholder
     * host (see `core:network`'s `build.gradle.kts`). `SyncManager` uses this to stay a genuine
     * no-op (no network calls at all) rather than pretending to sync against a host that doesn't
     * exist. Once a real API ships, pointing `API_BASE_URL` at it is the only change needed here.
     */
    val isConfigured: Boolean get() = baseUrl != PLACEHOLDER_BASE_URL

    companion object {
        const val PLACEHOLDER_BASE_URL = "https://api.expensee.example.com/"
    }
}
