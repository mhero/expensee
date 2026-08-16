package com.mac.expensee.feature.auth

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.feature.auth.domain.model.AuthSession
import com.mac.expensee.feature.auth.domain.model.User
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake used across auth unit tests -- no Room/Keystore involved. */
class FakeAuthRepository : AuthRepository {
    private val sessionFlow = MutableStateFlow<AuthSession?>(null)
    private val accounts = mutableMapOf<String, String>() // username -> password
    var biometricEnabled = false
        private set

    override fun observeSession() = sessionFlow

    override suspend fun hasAccount(): Boolean = accounts.isNotEmpty()

    override suspend fun register(username: String, password: CharArray): AppResult<AuthSession> {
        if (accounts.containsKey(username)) {
            return DataError.Validation.InvalidField("username", "taken").asError()
        }
        accounts[username] = String(password)
        val session = AuthSession(User(username, username, false), token = "token-$username")
        sessionFlow.value = session
        return session.asSuccess()
    }

    override suspend fun login(username: String, password: CharArray): AppResult<AuthSession> {
        val stored = accounts[username] ?: return DataError.Auth.InvalidCredentials.asError()
        if (stored != String(password)) return DataError.Auth.InvalidCredentials.asError()
        val session = AuthSession(User(username, username, biometricEnabled), token = "token-$username")
        sessionFlow.value = session
        return session.asSuccess()
    }

    override suspend fun restoreSessionAfterBiometricUnlock(): AppResult<AuthSession> =
        sessionFlow.value?.asSuccess() ?: DataError.Auth.SessionExpired.asError()

    override suspend fun logout() {
        sessionFlow.value = null
    }

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        biometricEnabled = enabled
    }
}
