package com.mac.expensee.core.network.dto

import kotlinx.serialization.Serializable

/** Batched push payload a future SyncWorker would send: everything changed locally since the last sync. */
@Serializable
data class SyncPushRequestDto(
    val expenses: List<ExpenseDto>,
    val categories: List<CategoryDto>,
    val deletedExpenseIds: List<String>,
)

@Serializable
data class SyncPullResponseDto(
    val expenses: List<ExpenseDto>,
    val categories: List<CategoryDto>,
    val serverTimestamp: Long,
)
