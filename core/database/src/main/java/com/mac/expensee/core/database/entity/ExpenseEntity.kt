package com.mac.expensee.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mac.expensee.core.common.sync.SyncStatus

/**
 * Local source of truth for a single expense.
 *
 * Sync metadata design (kept intentionally minimal -- see project README "Sync model"):
 * - [localId]: client-generated UUID, primary key, stable identity even before the record has
 *   ever reached a server. Never reused.
 * - [remoteId]: server-assigned id once synced; null for anything created offline and not yet
 *   pushed. Lets a future SyncWorker distinguish "create" vs "update" remote calls.
 * - [createdAt] / [updatedAt]: epoch millis, used both for display and as the basis of
 *   last-write-wins conflict resolution once a remote API exists.
 * - [syncStatus]: drives what SyncWorker does with the row (push, ignore, retry). See [SyncStatus].
 * - [deletedAt]: soft delete. Rows are tombstoned (not hard-deleted) until a delete is confirmed
 *   synced, so a delete that happened offline can still be pushed later. A cleanup worker purges
 *   tombstones once [syncStatus] is SYNCED and enough time has passed.
 * - [version]: monotonically incremented on every local edit; sent with sync requests so the
 *   server can detect a lost-update conflict (version mismatch) rather than blindly overwriting.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["localId"],
            childColumns = ["categoryLocalId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryLocalId"), Index("date"), Index("syncStatus")],
)
data class ExpenseEntity(
    @PrimaryKey val localId: String,
    val remoteId: String?,
    val categoryLocalId: String,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val description: String,
    val notes: String?,
    /** The date the expense occurred (user-chosen), epoch millis at start of day, UTC. */
    val date: Long,
    /** Content-URI or internal file path to the receipt image, resolved via ReceiptStorage. */
    val receiptUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    val deletedAt: Long?,
    val version: Int,
)
