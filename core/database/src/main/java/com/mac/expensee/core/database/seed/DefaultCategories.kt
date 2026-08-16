package com.mac.expensee.core.database.seed

import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.CategoryEntity
import java.util.UUID

/**
 * Categories every new install starts with. Seeded once, on database creation, by
 * [com.mac.expensee.core.database.AppDatabase]'s `RoomDatabase.Callback`. Users can rename or
 * delete these like any other category once they exist (see `isDefault`, used only to decide
 * pre-selection, never to block edits).
 */
object DefaultCategories {

    fun seedEntities(now: Long): List<CategoryEntity> = listOf(
        "Food & Dining" to "#EF6C00",
        "Transport" to "#1E88E5",
        "Housing" to "#8E24AA",
        "Utilities" to "#00897B",
        "Shopping" to "#D81B60",
        "Health" to "#43A047",
        "Entertainment" to "#FB8C00",
        "Other" to "#6D4C41",
    ).map { (name, color) ->
        CategoryEntity(
            localId = UUID.randomUUID().toString(),
            remoteId = null,
            name = name,
            colorHex = color,
            icon = "default",
            isDefault = true,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_UPLOAD,
            deletedAt = null,
            version = 1,
        )
    }
}
