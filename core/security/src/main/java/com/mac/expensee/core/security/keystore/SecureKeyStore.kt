package com.mac.expensee.core.security.keystore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encryption (reversible), distinct from [com.mac.expensee.core.security.password.PasswordHasher]
 * (one-way hashing). Backs the session token and any other small secret that must be decrypted
 * again later, using a key that never leaves the Android Keystore's secure hardware (or TEE/
 * software fallback on devices without it) -- the app process only ever handles ciphertext.
 *
 * Ordinary app data (expenses, categories) intentionally does NOT go through this class: it lives
 * in plain Room, matching the security model described in the project README ("what is and isn't
 * encrypted"). Encrypting the whole expenses table would need a passphrase-derived SQLCipher key
 * prompted on every cold start, which is unnecessary for a local expense tracker's threat model.
 */
class SecureKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? = prefs.getString(key, null)

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        private const val FILE_NAME = "expensee_secure_prefs"
        const val KEY_SESSION_TOKEN = "session_token"
    }
}
