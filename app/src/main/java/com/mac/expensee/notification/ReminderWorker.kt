package com.mac.expensee.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

/**
 * Deliberately re-checks the notifications-enabled setting itself (rather than trusting that it
 * was only ever enqueued while enabled) -- [ReminderScheduler.apply] cancels the periodic work
 * when the setting flips off, but a job already handed to the OS scheduler can still fire in the
 * narrow race between "user disabled it" and "cancellation took effect".
 */
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val reminderNotifier: ReminderNotifier,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.observeSettings().first()
        if (settings.notificationsEnabled) {
            reminderNotifier.showReminder()
        }
        return Result.success()
    }
}
