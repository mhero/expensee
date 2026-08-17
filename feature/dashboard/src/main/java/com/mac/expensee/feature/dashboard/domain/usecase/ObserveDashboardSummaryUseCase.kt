package com.mac.expensee.feature.dashboard.domain.usecase

import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import com.mac.expensee.feature.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

class ObserveDashboardSummaryUseCase(private val repository: DashboardRepository) {
    operator fun invoke(monthStartInclusive: Long, monthEndInclusive: Long): Flow<DashboardSummary> =
        repository.observeMonthlySummary(monthStartInclusive, monthEndInclusive)
}
