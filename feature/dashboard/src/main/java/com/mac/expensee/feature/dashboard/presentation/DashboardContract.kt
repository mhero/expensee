package com.mac.expensee.feature.dashboard.presentation

import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary

data class DashboardUiState(
    val summary: UiState<DashboardSummary> = UiState.Loading,
)
