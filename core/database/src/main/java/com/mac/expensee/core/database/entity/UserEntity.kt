package com.mac.expensee.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local account/profile. This app has exactly one local user at a time (single-device,
 * offline-first, no multi-tenant login yet) but is modeled as a table -- rather than a single
 * DataStore blob -- so a future RemoteAuthRepository can persist multiple synced accounts
 * without a schema change.
 *
 * [passwordHash] / [passwordSalt]: PBKDF2-derived, never the plaintext password. See
 * `core:security` `PasswordHasher` for the algorithm.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val localId: String,
    val remoteId: String?,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val biometricUnlockEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
