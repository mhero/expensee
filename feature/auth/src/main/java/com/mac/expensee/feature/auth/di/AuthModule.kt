package com.mac.expensee.feature.auth.di

import com.mac.expensee.core.security.biometric.BiometricUnlockRepository
import com.mac.expensee.feature.auth.data.LocalAuthRepository
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import com.mac.expensee.feature.auth.domain.usecase.HasAccountUseCase
import com.mac.expensee.feature.auth.domain.usecase.LoginUseCase
import com.mac.expensee.feature.auth.domain.usecase.LogoutUseCase
import com.mac.expensee.feature.auth.domain.usecase.ObserveSessionUseCase
import com.mac.expensee.feature.auth.domain.usecase.RegisterUseCase
import com.mac.expensee.feature.auth.presentation.login.LoginViewModel
import com.mac.expensee.feature.auth.presentation.setup.SetupAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    // Bound under both types: feature:settings depends only on the narrower BiometricUnlockRepository
    // (see its KDoc for why), while everything auth-related depends on AuthRepository. Same instance.
    single<AuthRepository> {
        LocalAuthRepository(userDao = get(), passwordHasher = get(), secureKeyStore = get())
    } bind BiometricUnlockRepository::class

    factory { RegisterUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ObserveSessionUseCase(get()) }
    factory { HasAccountUseCase(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { SetupAccountViewModel(get()) }
}
