package com.mac.expensee.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mac.expensee.feature.auth.domain.usecase.HasAccountUseCase
import com.mac.expensee.feature.auth.domain.usecase.LogoutUseCase
import com.mac.expensee.feature.auth.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Decides which nav graph the app should show: auth (login/setup) vs the authenticated app
 * graph. Lives at the app level -- rather than in `feature:auth` -- because it's the one place
 * that legitimately needs to know both "is there a session" (auth) and "what's the app's home
 * destination" (a decision that will eventually span multiple features).
 */
class RootViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    private val hasAccountUseCase: HasAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    val session = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _hasAccount = MutableStateFlow<Boolean?>(null)
    val hasAccount: StateFlow<Boolean?> = _hasAccount.asStateFlow()

    init {
        viewModelScope.launch {
            _hasAccount.value = hasAccountUseCase()
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}
