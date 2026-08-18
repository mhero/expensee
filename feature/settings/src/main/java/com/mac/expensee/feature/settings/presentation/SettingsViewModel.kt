package com.mac.expensee.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.feature.settings.domain.usecase.ObserveSettingsUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetBiometricUnlockEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetCurrencyUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetNotificationsEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Notably does NOT depend on [com.mac.expensee.core.security.biometric.BiometricAuthenticator]:
 * actually *prompting* biometrics needs a `FragmentActivity`, which a ViewModel must never hold a
 * reference to. Instead `SettingsScreen` runs the prompt itself and only calls
 * [SettingsAction.BiometricToggled] once the user has already proven they can authenticate --
 * this ViewModel's job is solely to persist that outcome.
 */
class SettingsViewModel(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setNotificationsEnabledUseCase: SetNotificationsEnabledUseCase,
    private val setBiometricUnlockEnabledUseCase: SetBiometricUnlockEnabledUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeSettingsUseCase()
            .onEach { settings ->
                _state.update {
                    it.copy(
                        currency = settings.currency,
                        theme = settings.theme,
                        notificationsEnabled = settings.notificationsEnabled,
                        biometricUnlockEnabled = settings.biometricUnlockEnabled,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.CurrencyChanged -> viewModelScope.launch { setCurrencyUseCase(action.currency) }
            is SettingsAction.ThemeChanged -> viewModelScope.launch { setThemeUseCase(action.theme) }
            is SettingsAction.NotificationsToggled -> viewModelScope.launch {
                setNotificationsEnabledUseCase(action.enabled)
            }
            is SettingsAction.BiometricToggled -> viewModelScope.launch {
                setBiometricUnlockEnabledUseCase(action.enabled)
            }
        }
    }
}
