package com.mac.expensee.feature.expenses.domain.model

import com.mac.expensee.core.common.money.Money

/**
 * These three types exist alongside [Expense] rather than extending it because sync bookkeeping
 * (`remoteId`, `updatedAt`, `version`) is exactly the persistence detail [Expense] is designed to
 * NOT carry (see that class's KDoc) -- the UI/domain use cases never need it, only
 * `ExpenseSyncGateway` and its one caller, `com.mac.expensee.sync.SyncManager`, do.
 */

/** A locally-created-or-edited expense waiting to be pushed. */
data class PendingExpenseUpload(
    val localId: String,
    val remoteId: String?,
    val categoryId: String,
    val amount: Money,
    val description: String,
    val notes: String?,
    val date: Long,
    val updatedAt: Long,
    val version: Int,
)

/** A locally-deleted (tombstoned) expense waiting for its deletion to be pushed. */
data class PendingExpenseDeletion(val localId: String, val remoteId: String?)

/** An expense as returned by a pull from the remote API, before it's reconciled into Room. */
data class RemoteExpenseSnapshot(
    val remoteId: String,
    val categoryId: String,
    val amount: Money,
    val description: String,
    val notes: String?,
    val date: Long,
    val updatedAt: Long,
    val version: Int,
)
