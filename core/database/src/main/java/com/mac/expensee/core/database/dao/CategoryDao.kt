package com.mac.expensee.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE deletedAt IS NULL ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE localId = :localId")
    suspend fun getById(localId: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * Soft-delete: mirrors [com.mac.expensee.core.database.dao.ExpenseDao.softDelete] -- a category
     * that was already pushed to a (future) server needs its deletion communicated, not silently
     * dropped locally. [hardDelete] still exists for the (today, hypothetical) case of purging a
     * tombstone once that deletion is confirmed synced.
     */
    @Query(
        "UPDATE categories SET deletedAt = :deletedAt, syncStatus = :syncStatus, updatedAt = :deletedAt, " +
            "version = version + 1 WHERE localId = :localId",
    )
    suspend fun softDelete(localId: String, deletedAt: Long, syncStatus: SyncStatus = SyncStatus.PENDING_DELETE)

    @Query("DELETE FROM categories WHERE localId = :localId")
    suspend fun hardDelete(localId: String)

    /** Hard-delete rows that were tombstoned and already confirmed synced -- used by cleanup work. */
    @Query("DELETE FROM categories WHERE deletedAt IS NOT NULL AND syncStatus = :synced")
    suspend fun purgeSyncedTombstones(synced: SyncStatus = SyncStatus.SYNCED)
}
