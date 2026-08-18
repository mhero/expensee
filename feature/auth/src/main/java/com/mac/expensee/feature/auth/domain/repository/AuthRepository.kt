package com.mac.expensee.feature.auth.domain.repository

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.security.biometric.BiometricUnlockRepository
import com.mac.expensee.feature.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction the presentation layer depends on. Today this is backed only by
 * [com.mac.expensee.feature.auth.data.LocalAuthRepository]; a future `RemoteAuthRepository`
 * (delegating to `core:network`'s `AuthApi`) can be swapped in -- or composed with the local one
 * for offline fallback -- without any ViewModel or Composable changing.
 *
 * Extends [BiometricUnlockRepository] so `feature:settings` can toggle/observe biometric unlock
 * through that narrower core-level interface without depending on this (feature:auth-owned) one.
 */
interface AuthRepository : BiometricUnlockRepository {

    /** Emits the current session, or null when logged out. Replayed to new collectors. */
    fun observeSession(): Flow<AuthSession?>

    /** Whether a local account has ever been created, i.e. whether to show setup vs login. */
    suspend fun hasAccount(): Boolean

    suspend fun register(username: String, password: CharArray): AppResult<AuthSession>

    suspend fun login(username: String, password: CharArray): AppResult<AuthSession>

    /**
     * Re-establishes the session after the UI has already confirmed the user's identity via
     * [com.mac.expensee.core.security.biometric.BiometricAuthenticator]. This method trusts the
     * caller: it does not itself invoke biometrics, since that requires an Activity the domain/data
     * layer must not depend on.
     */
    suspend fun restoreSessionAfterBiometricUnlock(): AppResult<AuthSession>

    suspend fun logout()
}
