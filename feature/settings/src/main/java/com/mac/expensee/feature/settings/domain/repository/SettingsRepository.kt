package com.mac.expensee.feature.settings.domain.repository

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.settings.domain.model.AppSettings
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Abstracts over where each preference actually lives (DataStore vs. the local account row --
 * see [AppSettings]'s KDoc). Presentation code and other features that will eventually read
 * preferences (e.g. defaulting a new expense's currency) depend only on this.
 */
interface SettingsRepository {

    fun observeSettings(): Flow<AppSettings>

    suspend fun setCurrency(currency: CurrencyCode)

    suspend fun setTheme(theme: ThemeMode)

    suspend fun setNotificationsEnabled(enabled: Boolean)

    suspend fun setBiometricUnlockEnabled(enabled: Boolean)
}
