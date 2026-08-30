package com.mac.expensee.core.security.biometric

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "expensee_biometric_unlock_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"

/** Discarded immediately after [Cipher.doFinal] succeeds -- see [BiometricAuthenticator]'s KDoc for why running the cipher matters more than its output. */
private val CONFIRMATION_PLAINTEXT = "expensee_biometric_confirmation".toByteArray()

/**
 * Thin wrapper around [BiometricPrompt] used to unlock the app after the initial PIN/password
 * login. Authentication is backed by an Android Keystore [SecretKey] rather than a bare
 * `prompt.authenticate(info)` call, so a successful callback means the OS actually unlocked a
 * hardware-backed key -- not just that some UI reported success. That guarantee is why every
 * check here targets [BiometricManager.Authenticators.BIOMETRIC_STRONG]: Android only allows
 * Class 3/"strong" biometrics to authorize a Keystore key at all, at any API level, so a device
 * with only weak-class biometrics (some basic face-unlock implementations) genuinely cannot use
 * this feature. [availability] reports that honestly rather than saying "available" for a device
 * where [authenticate] would always then fail.
 */
class BiometricAuthenticator {

    fun availability(manager: BiometricManager): BiometricAvailability =
        when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
            else -> BiometricAvailability.Unavailable
        }

    fun authenticate(activity: FragmentActivity, title: String, subtitle: String): Flow<BiometricResult> = callbackFlow {
        val cipher = try {
            encryptCipher()
        } catch (e: KeyPermanentlyInvalidatedException) {
            // The key is invalidated the moment the device's enrolled biometrics change (a
            // fingerprint or face gets added/removed) -- that's Android's own security guarantee
            // for auth-bound keys, not a bug. Delete the stale entry so the next attempt
            // regenerates a fresh key against whatever is enrolled now, and ask the user to retry.
            deleteKey()
            trySend(BiometricResult.Error("Biometric enrollment changed -- please try again"))
            close()
            return@callbackFlow
        } catch (e: GeneralSecurityException) {
            trySend(BiometricResult.Error(e.message ?: "Could not prepare biometric authentication"))
            close()
            return@callbackFlow
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // The callback firing only proves the OS reported success. Actually running the
                // returned cipher forces the operation through the hardware-backed key itself, so
                // a corrupted/tampered key state surfaces as a real failure here instead of a
                // false Success -- the plaintext and ciphertext are otherwise meaningless and
                // discarded immediately; nothing in the app is actually protected by this data.
                val verified = try {
                    result.cryptoObject?.cipher?.doFinal(CONFIRMATION_PLAINTEXT) != null
                } catch (e: GeneralSecurityException) {
                    false
                }
                if (verified) {
                    trySend(BiometricResult.Success)
                } else {
                    trySend(BiometricResult.Error("Authentication could not be verified"))
                }
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
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use password")
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
        awaitClose { }
    }

    private fun encryptCipher(): Cipher {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val key = (keyStore.getKey(KEY_ALIAS, null) as? SecretKey) ?: generateKey()
        return Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key) }
    }

    @Suppress("DEPRECATION") // see the validity-duration comment below
    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            // -1 means "require a fresh authentication for every single use of this key" -- the
            // API 30 replacement, setUserAuthenticationParameters(...), would force this whole
            // class to API 30+, but this project's minSdk is 26, and this deprecated overload
            // still means exactly the same thing back to API 23.
            .setUserAuthenticationValidityDurationSeconds(-1)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun deleteKey() {
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        }
    }
}