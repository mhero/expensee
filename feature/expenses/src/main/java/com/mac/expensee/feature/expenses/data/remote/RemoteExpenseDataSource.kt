package com.mac.expensee.feature.expenses.data.remote

import com.mac.expensee.core.network.dto.SyncPullResponseDto
import com.mac.expensee.core.network.dto.SyncPushRequestDto

/**
 * Thin wrapper over `core.network.api.ExpenseApi`. Not called by [com.mac.expensee.feature.expenses.data.ExpenseRepositoryImpl]
 * or anything else yet -- there is no backend. It exists so a future `SyncWorker` has a ready-made
 * seam to push/pull through, matching the project's "sync architecture exists, sync doesn't run" goal.
 */
interface RemoteExpenseDataSource {
    suspend fun push(request: SyncPushRequestDto): SyncPullResponseDto
    suspend fun pull(sinceEpochMillis: Long): SyncPullResponseDto
}

class RetrofitRemoteExpenseDataSource(
    private val api: com.mac.expensee.core.network.api.ExpenseApi,
) : RemoteExpenseDataSource {
    override suspend fun push(request: SyncPushRequestDto): SyncPullResponseDto {
        val updated = api.push(request)
        return SyncPullResponseDto(expenses = updated, categories = emptyList(), serverTimestamp = System.currentTimeMillis())
    }

    override suspend fun pull(sinceEpochMillis: Long): SyncPullResponseDto = api.pull(sinceEpochMillis)
}
