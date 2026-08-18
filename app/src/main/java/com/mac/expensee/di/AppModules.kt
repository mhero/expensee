package com.mac.expensee.di

import com.mac.expensee.core.common.di.commonModule
import com.mac.expensee.core.database.di.databaseModule
import com.mac.expensee.core.datastore.di.dataStoreModule
import com.mac.expensee.core.network.di.networkModule
import com.mac.expensee.core.security.di.securityModule
import com.mac.expensee.feature.auth.di.authModule
import com.mac.expensee.feature.categories.di.categoriesModule
import com.mac.expensee.feature.dashboard.di.dashboardModule
import com.mac.expensee.feature.expenses.di.expensesModule
import com.mac.expensee.feature.settings.di.settingsModule
import com.mac.expensee.navigation.RootViewModel
import com.mac.expensee.theme.AppThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val appModule = module {
    viewModel { RootViewModel(observeSessionUseCase = get(), hasAccountUseCase = get(), logoutUseCase = get()) }
    viewModel { AppThemeViewModel(observeSettingsUseCase = get()) }
}

/**
 * All Koin modules the app starts with. Each `core:*` / `feature:*` module owns and exports its
 * own module (see e.g. `core.security.di.securityModule`); the app module's only job is to know
 * they all exist and list them here -- no feature module depends on another feature module's DI.
 */
val appModules = listOf(
    commonModule,
    databaseModule,
    networkModule,
    securityModule,
    dataStoreModule,
    authModule,
    expensesModule,
    categoriesModule,
    dashboardModule,
    settingsModule,
    notificationModule,
    appModule,
)
