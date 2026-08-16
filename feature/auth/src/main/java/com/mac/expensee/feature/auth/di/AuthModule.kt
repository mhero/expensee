package com.mac.expensee.feature.auth.di

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
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { LocalAuthRepository(userDao = get(), passwordHasher = get(), secureKeyStore = get()) }

    factory { RegisterUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ObserveSessionUseCase(get()) }
    factory { HasAccountUseCase(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { SetupAccountViewModel(get()) }
}
