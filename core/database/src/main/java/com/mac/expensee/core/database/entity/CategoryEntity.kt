package com.mac.expensee.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mac.expensee.core.common.sync.SyncStatus

/**
 * A spending category. Sync metadata mirrors [ExpenseEntity] -- see that file for the rationale
 * behind each field.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val localId: String,
    val remoteId: String?,
    val name: String,
    val colorHex: String,
    val icon: String,
    val isDefault: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    val deletedAt: Long?,
    val version: Int,
)
