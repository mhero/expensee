package com.mac.expensee.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao

/**
 * Maintenance work, not any single feature's business logic -- "purge old tombstones" isn't a
 * CRUD operation any UI screen needs, so it reaches into [ExpenseDao]/[CategoryDao] directly
 * (both are already app-level-visible via `core:database`, the same way `app/notification`'s
 * `ReminderWorker` reaches into a feature *repository* for its business logic; this has none).
 *
 * A tombstone (`deletedAt != null`) only reaches `SyncStatus.SYNCED` once its deletion has been
 * confirmed pushed -- see [ExpenseDao.purgeSyncedTombstones]'s KDoc -- so this worker is itself a
 * safe no-op today: nothing is ever marked synced while there's no backend to sync with.
 */
class CleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        expenseDao.purgeSyncedTombstones()
        categoryDao.purgeSyncedTombstones()
        return Result.success()
    }
}
