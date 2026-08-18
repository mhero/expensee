package com.mac.expensee.feature.settings.domain.usecase

import com.mac.expensee.feature.settings.domain.repository.SettingsRepository

class SetBiometricUnlockEnabledUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = settingsRepository.setBiometricUnlockEnabled(enabled)
}
