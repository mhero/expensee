package com.mac.expensee.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * `schedule()` is idempotent ([ExistingPeriodicWorkPolicy.KEEP]) so it's safe to call both from
 * [com.mac.expensee.ExpenseeApplication.onCreate] on every app start and from
 * [com.mac.expensee.BootCompletedReceiver] after a reboot -- neither call disturbs an
 * already-scheduled job's timing.
 */
class SyncScheduler(private val context: Context) {

    fun schedule() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "expense_sync"
        private const val SYNC_INTERVAL_HOURS = 6L
    }
}
