package com.mac.expensee.core.security.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

sealed interface BiometricAvailability {
    data object Available : BiometricAvailability
    data object NoHardware : BiometricAvailability
    data object NoneEnrolled : BiometricAvailability
    data object Unavailable : BiometricAvailability
}

sealed interface BiometricResult {
    data object Success : BiometricResult
    data class Error(val message: String) : BiometricResult
    data object Cancelled : BiometricResult
}

/** Thin wrapper around [BiometricPrompt] used to unlock the app after the initial PIN/password login. */
class BiometricAuthenticator {

    fun availability(manager: BiometricManager): BiometricAvailability =
        when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
            else -> BiometricAvailability.Unavailable
        }

    fun authenticate(activity: FragmentActivity, title: String, subtitle: String) = callbackFlow {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                trySend(BiometricResult.Success)
                close()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    trySend(BiometricResult.Cancelled)
                } else {
                    trySend(BiometricResult.Error(errString.toString()))
                }
                close()
            }

            override fun onAuthenticationFailed() {
                // A single failed scan; the prompt stays open for another attempt, so no event here.
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setNegativeButtonText("Use password")
            .build()
        prompt.authenticate(info)
        awaitClose { }
    }
}
