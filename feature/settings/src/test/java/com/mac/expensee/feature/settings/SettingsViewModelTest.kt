package com.mac.expensee.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.testing.MainDispatcherRule
import com.mac.expensee.feature.settings.domain.model.AppSettings
import com.mac.expensee.feature.settings.domain.model.ThemeMode
import com.mac.expensee.feature.settings.domain.usecase.ObserveSettingsUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetBiometricUnlockEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetCurrencyUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetNotificationsEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetThemeUseCase
import com.mac.expensee.feature.settings.presentation.SettingsAction
import com.mac.expensee.feature.settings.presentation.SettingsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    private fun buildViewModel() = SettingsViewModel(
        observeSettingsUseCase = ObserveSettingsUseCase(repository),
        setCurrencyUseCase = SetCurrencyUseCase(repository),
        setThemeUseCase = SetThemeUseCase(repository),
        setNotificationsEnabledUseCase = SetNotificationsEnabledUseCase(repository),
        setBiometricUnlockEnabledUseCase = SetBiometricUnlockEnabledUseCase(repository),
    )

    @Test
    fun `initial state reflects the repository's current settings`() = runTest {
        val repository = FakeSettingsRepository(
            initial = AppSettings(currency = CurrencyCode.EUR, theme = ThemeMode.DARK, notificationsEnabled = true),
        )
        val viewModel = SettingsViewModel(
            observeSettingsUseCase = ObserveSettingsUseCase(repository),
            setCurrencyUseCase = SetCurrencyUseCase(repository),
            setThemeUseCase = SetThemeUseCase(repository),
            setNotificationsEnabledUseCase = SetNotificationsEnabledUseCase(repository),
            setBiometricUnlockEnabledUseCase = SetBiometricUnlockEnabledUseCase(repository),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.currency).isEqualTo(CurrencyCode.EUR)
            assertThat(state.theme).isEqualTo(ThemeMode.DARK)
            assertThat(state.notificationsEnabled).isTrue()
            assertThat(state.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing currency updates state and persists through the repository`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(SettingsAction.CurrencyChanged(CurrencyCode.GBP))

        viewModel.state.test {
            assertThat(awaitItem().currency).isEqualTo(CurrencyCode.GBP)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(repository.current.value.currency).isEqualTo(CurrencyCode.GBP)
    }

    @Test
    fun `toggling notifications persists the new value`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(SettingsAction.NotificationsToggled(true))

        viewModel.state.test {
            assertThat(awaitItem().notificationsEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggling biometric unlock persists the new value`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(SettingsAction.BiometricToggled(true))

        viewModel.state.test {
            assertThat(awaitItem().biometricUnlockEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing theme persists the new value`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(SettingsAction.ThemeChanged(ThemeMode.LIGHT))

        viewModel.state.test {
            assertThat(awaitItem().theme).isEqualTo(ThemeMode.LIGHT)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
