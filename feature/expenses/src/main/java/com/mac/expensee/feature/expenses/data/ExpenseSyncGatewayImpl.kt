package com.mac.expensee.feature.expenses.data

import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.expenses.data.mapper.toPendingUpload
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseDeletion
import com.mac.expensee.feature.expenses.domain.model.RemoteExpenseSnapshot
import com.mac.expensee.feature.expenses.domain.repository.ExpenseSyncGateway
import java.util.UUID

/**
 * Talks to [ExpenseDao] directly, the same way [com.mac.expensee.feature.expenses.data.ExpenseRepositoryImpl]
 * does -- sync bookkeeping is a data-layer concern like any other, it just serves a different
 * caller (see [ExpenseSyncGateway]'s KDoc).
 */
class ExpenseSyncGatewayImpl(private val expenseDao: ExpenseDao) : ExpenseSyncGateway {

    override suspend fun pendingUploads() =
        expenseDao.getPendingSync()
            .filter { it.deletedAt == null && it.syncStatus == SyncStatus.PENDING_UPLOAD }
            .map { it.toPendingUpload() }

    override suspend fun pendingDeletions() =
        expenseDao.getPendingSync()
            .filter { it.deletedAt != null && it.syncStatus == SyncStatus.PENDING_DELETE }
            .map { PendingExpenseDeletion(localId = it.localId, remoteId = it.remoteId) }

    override suspend fun markUploaded(localId: String, remoteId: String, syncedAt: Long) {
        val entity = expenseDao.getById(localId) ?: return
        expenseDao.update(entity.copy(remoteId = remoteId, syncStatus = SyncStatus.SYNCED, updatedAt = syncedAt))
    }

    override suspend fun markDeletionConfirmed(localId: String) {
        val entity = expenseDao.getById(localId) ?: return
        expenseDao.update(entity.copy(syncStatus = SyncStatus.SYNCED))
    }

    override suspend fun applyRemoteSnapshot(snapshot: RemoteExpenseSnapshot) {
        val existing = expenseDao.getByRemoteId(snapshot.remoteId)
        if (existing != null && existing.updatedAt >= snapshot.updatedAt) {
            // Local change is newer (or exactly equal, i.e. this is the echo of our own last
            // push) -- keep it. If it's genuinely still unsynced, it'll be picked up as a
            // pending upload on the next sync pass rather than being overwritten here.
            return
        }
        // NOTE: `snapshot.categoryId` is left unmapped -- category sync itself isn't wired yet
        // (see com.mac.expensee.sync.SyncManager's KDoc), so there's no local<->remote category
        // id table to translate through. Reusing the existing local category (if any), or falling
        // back to the snapshot's raw id otherwise, is a deliberate placeholder for that gap, not
        // a design decision -- category sync lands this the same way expense sync did here.
        val entity = ExpenseEntity(
            localId = existing?.localId ?: UUID.randomUUID().toString(),
            remoteId = snapshot.remoteId,
            categoryLocalId = existing?.categoryLocalId ?: snapshot.categoryId,
            amountMinorUnits = snapshot.amount.amountMinorUnits,
            currencyCode = snapshot.amount.currency.isoCode,
            description = snapshot.description,
            notes = snapshot.notes,
            date = snapshot.date,
            receiptUri = existing?.receiptUri,
            createdAt = existing?.createdAt ?: snapshot.updatedAt,
            updatedAt = snapshot.updatedAt,
            syncStatus = SyncStatus.SYNCED,
            deletedAt = null,
            version = snapshot.version,
        )
        expenseDao.upsert(entity)
    }

    override suspend fun purgeSyncedTombstones() = expenseDao.purgeSyncedTombstones()
}
