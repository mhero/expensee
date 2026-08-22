package com.mac.expensee.feature.dashboard.di

import com.mac.expensee.feature.dashboard.data.DashboardRepositoryImpl
import com.mac.expensee.feature.dashboard.domain.repository.DashboardRepository
import com.mac.expensee.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.mac.expensee.feature.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    single<DashboardRepository> { DashboardRepositoryImpl(expenseDao = get(), categoryDao = get()) }
    factory { ObserveDashboardSummaryUseCase(get()) }
    viewModel { DashboardViewModel(get()) }
}
