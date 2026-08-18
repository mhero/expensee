package com.mac.expensee.di

import com.mac.expensee.notification.ReminderCoordinator
import com.mac.expensee.notification.ReminderNotifier
import com.mac.expensee.notification.ReminderScheduler
import com.mac.expensee.notification.ReminderWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

/**
 * Reminder scheduling/notification infra lives at the app level, not in `feature:settings`: it
 * depends on `SettingsRepository` (a feature abstraction) *and* on Android's WorkManager/
 * notification APIs, so it's cross-cutting composition rather than something `feature:settings`
 * itself owns. Mirrors how `RootViewModel` (also app-level) depends on `feature:auth`'s use cases.
 */
val notificationModule = module {
    single { ReminderNotifier(context = get()) }
    single { ReminderScheduler(context = get()) }
    single { ReminderCoordinator(settingsRepository = get(), scheduler = get()) }

    worker { params ->
        ReminderWorker(
            context = params.get(),
            workerParams = params.get(),
            settingsRepository = get(),
            reminderNotifier = get(),
        )
    }
}
