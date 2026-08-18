package com.mac.expensee

import android.app.Application
import com.mac.expensee.di.appModules
import com.mac.expensee.notification.ReminderCoordinator
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ExpenseeApplication : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@ExpenseeApplication)
            workManagerFactory()
            modules(appModules)
        }
        startReminderCoordinator()
    }

    /**
     * Keeps the reminder [androidx.work.WorkManager] job in sync with the notifications setting
     * for the whole app lifetime -- see [ReminderCoordinator]'s KDoc for why this isn't tied to
     * the Settings screen.
     */
    private fun startReminderCoordinator() {
        val coordinator: ReminderCoordinator = get()
        val appScope: CoroutineScope = get()
        coordinator.start(appScope)
    }
}
