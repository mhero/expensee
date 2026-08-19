package com.mac.expensee.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val syncManager: SyncManager,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = when (syncManager.sync()) {
        SyncManager.SyncOutcome.Skipped, SyncManager.SyncOutcome.Success -> Result.success()
        is SyncManager.SyncOutcome.Failure -> if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
    }

    private companion object {
        const val MAX_RETRIES = 5
    }
}
