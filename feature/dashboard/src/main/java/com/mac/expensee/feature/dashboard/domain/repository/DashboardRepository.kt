package com.mac.expensee.feature.dashboard.domain.repository

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.feature.dashboard.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

/**
 * Read-only, aggregated view over the same Room tables `feature:expenses` and `feature:categories`
 * use -- built independently (via `core:database` directly) rather than depending on either
 * feature module, per this project's "no feature depends on another feature" rule.
 */
interface DashboardRepository {
    /**
     * [preferredCurrency] is the UI-selected currency to scope the summary to (see
     * [DashboardSummary]'s KDoc for how a mismatch or null is resolved) -- pass null until the
     * user has picked one.
     */
    fun observeMonthlySummary(
        monthStartInclusive: Long,
        monthEndInclusive: Long,
        preferredCurrency: CurrencyCode?,
    ): Flow<DashboardSummary>
}
