package com.mac.expensee.feature.dashboard.domain.repository

import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

/**
 * Read-only, aggregated view over the same Room tables `feature:expenses` and `feature:categories`
 * use -- built independently (via `core:database` directly) rather than depending on either
 * feature module, per this project's "no feature depends on another feature" rule.
 */
interface DashboardRepository {
    fun observeMonthlySummary(monthStartInclusive: Long, monthEndInclusive: Long): Flow<DashboardSummary>
}
