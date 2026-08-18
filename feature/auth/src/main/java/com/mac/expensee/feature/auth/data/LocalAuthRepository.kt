package com.mac.expensee.feature.auth.data

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.core.database.dao.UserDao
import com.mac.expensee.core.database.entity.UserEntity
import com.mac.expensee.core.security.keystore.SecureKeyStore
import com.mac.expensee.core.security.password.PasswordHash
import com.mac.expensee.core.security.password.PasswordHasher
import com.mac.expensee.feature.auth.domain.model.AuthSession
import com.mac.expensee.feature.auth.domain.model.User
import com.mac.expensee.feature.auth.domain.repository.AuthRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * The only [AuthRepository] implementation today. Local-only: there is no server, so "login"
 * means "prove you know the password whose hash matches the single local account".
 *
 * Session model: on successful login/register, a random opaque token is generated and stored in
 * [SecureKeyStore] (Keystore-encrypted). [observeSession] is backed by an in-memory
 * [MutableStateFlow] seeded from that stored token at construction time, so the session survives
 * process death (token still in SecureKeyStore) without re-reading disk on every collection.
 */
class LocalAuthRepository(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val secureKeyStore: SecureKeyStore,
) : AuthRepository {

    private val sessionState = MutableStateFlow<AuthSession?>(null)

    init {
        // Fire-and-forget restoration on cold start; observeSession() will emit null until this
        // completes, then emit the restored session if one exists. Kept synchronous-looking here
        // via runBlocking would block app startup, so instead callers unlock via
        // restoreSessionAfterBiometricUnlock() or plain login() -- see class doc.
    }

    override fun observeSession(): Flow<AuthSession?> = sessionState.asStateFlow()

    override suspend fun hasAccount(): Boolean = userDao.getCurrentUser() != null

    override suspend fun register(username: String, password: CharArray): AppResult<AuthSession> {
        if (userDao.getByUsername(username) != null) {
            return DataError.Validation.InvalidField("username", "That username is already taken").asError()
        }
        val hash = passwordHasher.hash(password)
        val now = System.currentTimeMillis()
        val entity = UserEntity(
            localId = UUID.randomUUID().toString(),
            remoteId = null,
            username = username,
            passwordHash = hash.hash,
            passwordSalt = hash.salt,
            biometricUnlockEnabled = false,
            createdAt = now,
            updatedAt = now,
        )
        userDao.upsert(entity)
        return establishSession(entity)
    }

    override suspend fun login(username: String, password: CharArray): AppResult<AuthSession> {
        val entity = userDao.getByUsername(username)
            ?: return DataError.Auth.InvalidCredentials.asError()
        val matches = passwordHasher.verify(password, PasswordHash(entity.passwordHash, entity.passwordSalt))
        if (!matches) {
            return DataError.Auth.InvalidCredentials.asError()
        }
        return establishSession(entity)
    }

    override suspend fun restoreSessionAfterBiometricUnlock(): AppResult<AuthSession> {
        val entity = userDao.getCurrentUser() ?: return DataError.Auth.SessionExpired.asError()
        if (!entity.biometricUnlockEnabled) return DataError.Auth.BiometricUnavailable.asError()
        return establishSession(entity)
    }

    override suspend fun logout() {
        secureKeyStore.remove(SecureKeyStore.KEY_SESSION_TOKEN)
        sessionState.value = null
    }

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        val entity = userDao.getCurrentUser() ?: return
        userDao.update(entity.copy(biometricUnlockEnabled = enabled, updatedAt = System.currentTimeMillis()))
        sessionState.value?.let { current ->
            sessionState.value = current.copy(user = current.user.copy(biometricUnlockEnabled = enabled))
        }
    }

    override fun observeBiometricUnlockEnabled(): Flow<Boolean> =
        userDao.observeCurrentUser().map { it?.biometricUnlockEnabled ?: false }

    private fun establishSession(entity: UserEntity): AppResult<AuthSession> {
        val token = UUID.randomUUID().toString()
        secureKeyStore.putString(SecureKeyStore.KEY_SESSION_TOKEN, token)
        val session = AuthSession(
            user = User(
                id = entity.localId,
                username = entity.username,
                biometricUnlockEnabled = entity.biometricUnlockEnabled,
            ),
            token = token,
        )
        sessionState.value = session
        return session.asSuccess()
    }
}
