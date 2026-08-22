package com.mac.expensee.feature.settings.di

import com.mac.expensee.feature.settings.data.SettingsRepositoryImpl
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import com.mac.expensee.feature.settings.domain.usecase.ObserveSettingsUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetBiometricUnlockEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetCurrencyUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetNotificationsEnabledUseCase
import com.mac.expensee.feature.settings.domain.usecase.SetThemeUseCase
import com.mac.expensee.feature.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(preferencesDataSource = get(), biometricUnlockRepository = get())
    }

    factory { ObserveSettingsUseCase(get()) }
    factory { SetCurrencyUseCase(get()) }
    factory { SetThemeUseCase(get()) }
    factory { SetNotificationsEnabledUseCase(get()) }
    factory { SetBiometricUnlockEnabledUseCase(get()) }

    viewModel {
        SettingsViewModel(
            observeSettingsUseCase = get(),
            setCurrencyUseCase = get(),
            setThemeUseCase = get(),
            setNotificationsEnabledUseCase = get(),
            setBiometricUnlockEnabledUseCase = get(),
        )
    }
}
