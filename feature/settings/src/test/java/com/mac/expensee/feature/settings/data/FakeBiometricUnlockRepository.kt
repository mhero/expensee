package com.mac.expensee.feature.settings.data

import com.mac.expensee.core.security.biometric.BiometricUnlockRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBiometricUnlockRepository(initial: Boolean = false) : BiometricUnlockRepository {

    private val enabled = MutableStateFlow(initial)

    override fun observeBiometricUnlockEnabled() = enabled

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        this.enabled.value = enabled
    }
}
