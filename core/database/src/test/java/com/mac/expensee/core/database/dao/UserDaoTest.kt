package com.mac.expensee.core.database.dao

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.database.entity.UserEntity
import com.mac.expensee.core.testing.testFirst
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserDaoTest : RoomDaoTest() {

    private val userDao get() = db.userDao()

    private fun user(
        localId: String = "u1",
        username: String = "alice",
        biometricUnlockEnabled: Boolean = false,
    ) = UserEntity(
        localId = localId,
        remoteId = null,
        username = username,
        passwordHash = "hash",
        passwordSalt = "salt",
        biometricUnlockEnabled = biometricUnlockEnabled,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `observeCurrentUser emits null before any account exists`() = runTest {
        userDao.observeCurrentUser().testFirst { assertThat(it).isNull() }
    }

    @Test
    fun `observeCurrentUser emits the single local account once created`() = runTest {
        userDao.upsert(user())

        userDao.observeCurrentUser().testFirst { assertThat(it?.username).isEqualTo("alice") }
    }

    @Test
    fun `getByUsername finds the matching account, case-sensitively`() = runTest {
        userDao.upsert(user(username = "alice"))

        assertThat(userDao.getByUsername("alice")?.localId).isEqualTo("u1")
        assertThat(userDao.getByUsername("Alice")).isNull()
    }

    @Test
    fun `upsert replaces the row for an existing localId`() = runTest {
        userDao.upsert(user(username = "alice"))

        userDao.upsert(user(username = "alice2"))

        assertThat(userDao.getCurrentUser()?.username).isEqualTo("alice2")
    }

    @Test
    fun `update persists changes to an existing account`() = runTest {
        val original = user()
        userDao.upsert(original)

        userDao.update(original.copy(biometricUnlockEnabled = true))

        assertThat(userDao.getCurrentUser()?.biometricUnlockEnabled).isTrue()
    }
}
