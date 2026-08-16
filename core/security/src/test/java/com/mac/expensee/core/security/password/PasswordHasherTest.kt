package com.mac.expensee.core.security.password

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [PasswordHasher.hash] uses `android.util.Base64`, which only works under Robolectric (or a real
 * device) rather than a plain JVM unit test.
 */
@RunWith(RobolectricTestRunner::class)
class PasswordHasherTest {

    @Test
    fun `same password with different salts produces different hashes`() {
        val hasher = PasswordHasher()
        val first = hasher.hash("correct horse battery".toCharArray())
        val second = hasher.hash("correct horse battery".toCharArray())
        assertThat(first.salt).isNotEqualTo(second.salt)
        assertThat(first.hash).isNotEqualTo(second.hash)
    }

    @Test
    fun `verify succeeds for the original password`() {
        val hasher = PasswordHasher()
        val hash = hasher.hash("correct horse battery".toCharArray())
        assertThat(hasher.verify("correct horse battery".toCharArray(), hash)).isTrue()
    }

    @Test
    fun `verify fails for a different password`() {
        val hasher = PasswordHasher()
        val hash = hasher.hash("correct horse battery".toCharArray())
        assertThat(hasher.verify("wrong password".toCharArray(), hash)).isFalse()
    }
}
