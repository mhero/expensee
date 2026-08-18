package com.mac.expensee.feature.settings.domain.model

/** App-wide theme preference. SYSTEM defers to [androidx.compose.foundation.isSystemInDarkTheme]. */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    ;

    companion object {
        fun fromStorageValue(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
