package com.mac.expensee.core.security.biometric

import kotlinx.coroutines.flow.Flow

/**
 * Narrow abstraction that exists purely so `feature:settings` can observe/toggle biometric
 * unlock without depending on `feature:auth` (no feature module depends on another -- see project
 * README). `feature:auth`'s `AuthRepository` extends this interface, and `LocalAuthRepository` is
 * the single implementation of both; Koin binds that one instance under both types.
 */
interface BiometricUnlockRepository {

    /** Whether biometric unlock is currently enabled for the local account, or false if none exists. */
    fun observeBiometricUnlockEnabled(): Flow<Boolean>

    suspend fun setBiometricUnlockEnabled(enabled: Boolean)
}
