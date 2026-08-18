package com.mac.expensee.feature.settings.presentation

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.settings.domain.model.ThemeMode

data class SettingsUiState(
    val currency: CurrencyCode = CurrencyCode.USD,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface SettingsAction {
    data class CurrencyChanged(val currency: CurrencyCode) : SettingsAction
    data class ThemeChanged(val theme: ThemeMode) : SettingsAction
    data class NotificationsToggled(val enabled: Boolean) : SettingsAction
    data class BiometricToggled(val enabled: Boolean) : SettingsAction
}
