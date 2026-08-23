package com.mac.expensee.feature.dashboard.presentation

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary

data class DashboardUiState(
    val summary: UiState<DashboardSummary> = UiState.Loading,
)

sealed interface DashboardAction {
    /** User picked a currency from `CurrencySelector` -- see [DashboardSummary]'s KDoc for how it's applied. */
    data class CurrencySelected(val currency: CurrencyCode) : DashboardAction
}
