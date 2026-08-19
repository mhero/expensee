package com.mac.expensee.feature.expenses.domain.repository

import com.mac.expensee.feature.expenses.domain.model.PendingExpenseDeletion
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseUpload
import com.mac.expensee.feature.expenses.domain.model.RemoteExpenseSnapshot

/**
 * Narrow seam between `feature:expenses` and the app-level `com.mac.expensee.sync.SyncManager`.
 * Deliberately NOT part of [ExpenseRepository]: that interface is "what the UI needs" (CRUD
 * scoped to [com.mac.expensee.feature.expenses.domain.model.Expense]); this is "what a sync
 * engine needs" (raw sync-status bookkeeping across all rows, synced or not) -- a different
 * concern with a different, non-UI caller. Keeping it separate also keeps `ExpenseRepository`
 * itself, and every existing use case built on it, completely unaware that sync exists.
 */
interface ExpenseSyncGateway {

    suspend fun pendingUploads(): List<PendingExpenseUpload>

    suspend fun pendingDeletions(): List<PendingExpenseDeletion>

    suspend fun markUploaded(localId: String, remoteId: String, syncedAt: Long)

    suspend fun markDeletionConfirmed(localId: String)

    /** Last-write-wins: a snapshot older than (or equal to) the local row's `updatedAt` is dropped. */
    suspend fun applyRemoteSnapshot(snapshot: RemoteExpenseSnapshot)

    suspend fun purgeSyncedTombstones()
}
