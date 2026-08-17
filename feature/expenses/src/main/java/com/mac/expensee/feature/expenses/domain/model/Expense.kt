package com.mac.expensee.feature.expenses.domain.model

import com.mac.expensee.core.common.money.Money

/**
 * Domain-layer expense. Deliberately has no sync metadata (localId vs remoteId, syncStatus,
 * version) -- that's a data-layer/persistence concern (see `core.database.entity.ExpenseEntity`)
 * that the UI and use cases never need to reason about. [id] is the entity's stable localId.
 */
data class Expense(
    val id: String,
    val categoryId: String,
    val amount: Money,
    val description: String,
    val notes: String?,
    /** Epoch millis, start-of-day UTC, of the date the expense occurred (not when it was entered). */
    val date: Long,
    /** Absolute local file path to the receipt image, or null if none was attached. */
    val receiptPath: String?,
)
