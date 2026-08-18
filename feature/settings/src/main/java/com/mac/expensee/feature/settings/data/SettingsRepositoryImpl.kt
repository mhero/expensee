package com.mac.expensee.feature.settings.data

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.datastore.UserPreferencesDataSource
import com.mac.expensee.core.security.biometric.BiometricUnlockRepository
import com.mac.expensee.feature.settings.domain.model.AppSettings
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The only [SettingsRepository] implementation. Combines three DataStore-backed flows with the
 * account's biometric-unlock flag (which intentionally lives with the local user row, not
 * DataStore -- see [AppSettings]) into one [AppSettings] stream so the UI observes a single
 * coherent state object rather than four separate ones.
 */
class SettingsRepositoryImpl(
    private val preferencesDataSource: UserPreferencesDataSource,
    private val biometricUnlockRepository: BiometricUnlockRepository,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = combine(
        preferencesDataSource.currencyCode,
        preferencesDataSource.themeMode,
        preferencesDataSource.notificationsEnabled,
        biometricUnlockRepository.observeBiometricUnlockEnabled(),
    ) { currencyCode, themeMode, notificationsEnabled, biometricEnabled ->
        AppSettings(
            currency = currencyCode?.let(CurrencyCode::fromIsoCode) ?: CurrencyCode.USD,
            theme = ThemeMode.fromStorageValue(themeMode),
            notificationsEnabled = notificationsEnabled ?: false,
            biometricUnlockEnabled = biometricEnabled,
        )
    }

    override suspend fun setCurrency(currency: CurrencyCode) {
        preferencesDataSource.setCurrencyCode(currency.isoCode)
    }

    override suspend fun setTheme(theme: ThemeMode) {
        preferencesDataSource.setThemeMode(theme.name)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferencesDataSource.setNotificationsEnabled(enabled)
    }

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        biometricUnlockRepository.setBiometricUnlockEnabled(enabled)
    }
}
