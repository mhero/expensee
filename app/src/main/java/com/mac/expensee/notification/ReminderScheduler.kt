package com.mac.expensee.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Enqueues/cancels the periodic reminder job. A daily cadence is a deliberately simple, fixed
 * choice for an expense tracker's reminder -- a user-configurable schedule would be
 * over-engineering for what this project demonstrates (see project README).
 */
class ReminderScheduler(private val context: Context) {

    fun apply(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        } else {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "expense_reminder"
    }
}
