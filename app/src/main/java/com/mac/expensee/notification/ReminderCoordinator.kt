package com.mac.expensee.notification

import com.mac.expensee.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Started once from [com.mac.expensee.ExpenseeApplication.onCreate] on the app's long-lived
 * coroutine scope (not tied to any screen's lifecycle) -- the reminder must stay scheduled, or
 * stay cancelled, even while the Settings screen isn't on screen.
 */
class ReminderCoordinator(
    private val settingsRepository: SettingsRepository,
    private val scheduler: ReminderScheduler,
) {
    fun start(scope: CoroutineScope) {
        settingsRepository.observeSettings()
            .map { it.notificationsEnabled }
            .distinctUntilChanged()
            .onEach { enabled -> scheduler.apply(enabled) }
            .launchIn(scope)
    }
}
