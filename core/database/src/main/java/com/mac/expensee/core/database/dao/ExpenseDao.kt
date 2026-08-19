package com.mac.expensee.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    /** All non-deleted expenses, most recent first. The UI observes this Flow directly. */
    @Query("SELECT * FROM expenses WHERE deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE deletedAt IS NULL AND categoryLocalId = :categoryLocalId " +
            "ORDER BY date DESC, createdAt DESC",
    )
    fun observeByCategory(categoryLocalId: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE deletedAt IS NULL AND date >= :fromInclusive AND date <= :toInclusive " +
            "ORDER BY date DESC, createdAt DESC",
    )
    fun observeInRange(fromInclusive: Long, toInclusive: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE localId = :localId")
    fun observeById(localId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE localId = :localId")
    suspend fun getById(localId: String): ExpenseEntity?

    /** Used by sync when applying a pulled remote change to find the matching local row, if any. */
    @Query("SELECT * FROM expenses WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): ExpenseEntity?

    @Query(
        "SELECT * FROM expenses WHERE deletedAt IS NULL AND date >= :fromInclusive AND date <= :toInclusive " +
            "ORDER BY date DESC LIMIT :limit",
    )
    fun observeRecent(fromInclusive: Long, toInclusive: Long, limit: Int): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    /** Soft-delete: marks for deletion instead of removing the row, so the delete can still sync. */
    @Query(
        "UPDATE expenses SET deletedAt = :deletedAt, syncStatus = :syncStatus, updatedAt = :deletedAt, " +
            "version = version + 1 WHERE localId = :localId",
    )
    suspend fun softDelete(localId: String, deletedAt: Long, syncStatus: SyncStatus = SyncStatus.PENDING_DELETE)

    /** Hard-delete rows that were tombstoned and already confirmed synced -- used by cleanup work. */
    @Query("DELETE FROM expenses WHERE deletedAt IS NOT NULL AND syncStatus = :synced")
    suspend fun purgeSyncedTombstones(synced: SyncStatus = SyncStatus.SYNCED)

    @Query("SELECT * FROM expenses WHERE syncStatus != :synced")
    suspend fun getPendingSync(synced: SyncStatus = SyncStatus.SYNCED): List<ExpenseEntity>

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryLocalId = :categoryLocalId AND deletedAt IS NULL")
    suspend fun countByCategory(categoryLocalId: String): Int
}
