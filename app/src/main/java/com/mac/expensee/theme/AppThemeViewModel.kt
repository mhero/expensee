package com.mac.expensee.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.feature.settings.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Lives at the app level (like [com.mac.expensee.navigation.RootViewModel]) rather than in
 * `feature:settings`, because applying the theme to the whole app's [androidx.compose.material3.MaterialTheme]
 * wrapper is an app-composition concern, not something the Settings screen itself needs.
 */
class AppThemeViewModel(observeSettingsUseCase: ObserveSettingsUseCase) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = observeSettingsUseCase()
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
}
