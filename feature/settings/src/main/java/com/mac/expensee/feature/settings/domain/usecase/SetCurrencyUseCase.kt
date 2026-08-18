package com.mac.expensee.feature.settings.domain.usecase

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository

class SetCurrencyUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(currency: CurrencyCode) = settingsRepository.setCurrency(currency)
}
