package com.mac.expensee.feature.settings.domain.model

import com.mac.expensee.core.common.money.CurrencyCode

/**
 * The full set of user-configurable app preferences. [biometricUnlockEnabled] is included here
 * for a single UI-facing read model even though, underneath, it's persisted via
 * [com.mac.expensee.core.security.biometric.BiometricUnlockRepository] (tied to the local account
 * row) rather than DataStore like the other three fields -- see [SettingsRepository][com.mac.expensee.feature.settings.domain.repository.SettingsRepository].
 */
data class AppSettings(
    val currency: CurrencyCode = CurrencyCode.USD,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
)
