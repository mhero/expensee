package com.mac.expensee.core.security.password

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Password hashing -- NOT encryption. A password hash is intentionally one-way: there is no key
 * that recovers the original password from [PasswordHash]. This is distinct from the
 * Keystore-backed encryption in [com.mac.expensee.core.security.keystore.SecureKeyStore], which
 * IS reversible for whoever holds the key.
 *
 * Uses PBKDF2WithHmacSHA256 (a standard, established JCE primitive -- no custom crypto), a
 * per-password random salt, and a cost factor high enough to resist offline brute force while
 * staying fast enough for interactive login on a phone.
 */
data class PasswordHash(val hash: String, val salt: String)

class PasswordHasher {

    fun hash(password: CharArray): PasswordHash {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hashBytes = deriveKey(password, salt)
        return PasswordHash(
            hash = Base64.encodeToString(hashBytes, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
        )
    }

    fun verify(password: CharArray, expected: PasswordHash): Boolean {
        val salt = Base64.decode(expected.salt, Base64.NO_WRAP)
        val candidate = deriveKey(password, salt)
        val expectedBytes = Base64.decode(expected.hash, Base64.NO_WRAP)
        return constantTimeEquals(candidate, expectedBytes)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /** Avoids short-circuiting comparison, which would otherwise leak timing information. */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
    }
}
