package com.mac.expensee.feature.settings.domain.usecase

import com.mac.expensee.feature.settings.domain.repository.SettingsRepository

class SetNotificationsEnabledUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = settingsRepository.setNotificationsEnabled(enabled)
}
