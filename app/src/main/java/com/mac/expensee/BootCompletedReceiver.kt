package com.mac.expensee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import com.mac.expensee.notification.ReminderScheduler
import com.mac.expensee.sync.CleanupScheduler
import com.mac.expensee.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Re-applies WorkManager's periodic jobs ([SyncScheduler], [CleanupScheduler], and the Phase 4
 * expense-reminder [ReminderScheduler]) after a device reboot.
 *
 * This is the project's demonstrated use of `BroadcastReceiver` (see project README's
 * "Notifications & background work" section). It's deliberately narrow: WorkManager itself
 * already persists periodic work across reboots on stock Android (it re-registers with
 * AlarmManager/JobScheduler the next time the app process starts), so in principle this receiver
 * is a defensive measure, not a strict requirement -- some OEM battery-optimization skins are
 * known to clear an app's scheduled jobs across a reboot before WorkManager gets a chance to
 * restore them. Long-running work itself belongs in `SyncWorker`/`CleanupWorker`/`ReminderWorker`
 * (all WorkManager `CoroutineWorker`s), never here -- this receiver only re-enqueues them.
 *
 * Manifest security: `android:exported="true"` is required for the system to deliver
 * `BOOT_COMPLETED` at all (it's a protected broadcast nothing but the system can send), and
 * `android:permission="android.permission.RECEIVE_BOOT_COMPLETED"` on the manifest entry
 * additionally restricts delivery to holders of that permission as defence in depth.
 */
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val syncScheduler: SyncScheduler = get()
        val cleanupScheduler: CleanupScheduler = get()
        val reminderScheduler: ReminderScheduler = get()
        val settingsRepository: SettingsRepository = get()

        // Reading whether reminders are enabled is a suspend DataStore read, which can't happen
        // directly in onReceive's short (~10s), main-thread time budget. goAsync() tells the
        // system this broadcast isn't finished until pendingResult.finish() is called, while the
        // actual work runs on a background dispatcher.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncScheduler.schedule()
                cleanupScheduler.schedule()
                val notificationsEnabled = settingsRepository.observeSettings().first().notificationsEnabled
                reminderScheduler.apply(notificationsEnabled)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
