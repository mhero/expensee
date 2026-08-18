package com.mac.expensee.feature.settings.domain.usecase

import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository

class SetThemeUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(theme: ThemeMode) = settingsRepository.setTheme(theme)
}
