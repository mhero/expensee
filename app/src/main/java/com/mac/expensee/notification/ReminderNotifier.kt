package com.mac.expensee.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mac.expensee.R

/**
 * Builds and shows the "log an expense" reminder notification. Kept deliberately dumb -- it has
 * no opinion on *whether* to notify (that's [ReminderWorker], driven by the notifications-enabled
 * setting) or *when* (WorkManager's periodic schedule, see [ReminderScheduler]); it only knows how.
 */
class ReminderNotifier(private val context: Context) {

    fun showReminder() {
        if (!hasPostNotificationsPermission()) {
            // Permission was revoked (or the notifications toggle raced ahead of a grant) after
            // this reminder was scheduled; skip rather than crash -- a background worker has no
            // way to prompt for permission mid-job.
            return
        }
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle("Don't forget to log today's expenses")
            .setContentText("A quick entry now keeps your dashboard accurate.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Expense reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Occasional reminders to record your expenses"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CHANNEL_ID = "expense_reminders"
        private const val NOTIFICATION_ID = 1001
    }
}
