package com.mac.expensee.feature.settings.domain.usecase

import com.mac.expensee.feature.settings.domain.model.AppSettings
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.observeSettings()
}
