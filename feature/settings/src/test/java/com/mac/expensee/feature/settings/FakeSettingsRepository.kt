package com.mac.expensee.feature.settings

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.settings.domain.model.AppSettings
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {

    private val settings = MutableStateFlow(initial)
    val current: StateFlow<AppSettings> = settings

    override fun observeSettings() = settings

    override suspend fun setCurrency(currency: CurrencyCode) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setTheme(theme: ThemeMode) {
        settings.value = settings.value.copy(theme = theme)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(notificationsEnabled = enabled)
    }

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(biometricUnlockEnabled = enabled)
    }
}
