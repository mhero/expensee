package com.mac.expensee.feature.expenses.data.mapper

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.expenses.domain.model.Expense

/**
 * Entity <-> domain mapping. Sync metadata (remoteId, syncStatus, deletedAt, version, timestamps)
 * is intentionally dropped when mapping to [Expense] and re-derived/preserved when mapping back --
 * the domain layer never carries persistence details, per the module's architecture rules.
 */
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = localId,
    categoryId = categoryLocalId,
    amount = Money(amountMinorUnits, CurrencyCode.fromIsoCode(currencyCode)),
    description = description,
    notes = notes,
    date = date,
    receiptPath = receiptUri,
)

/**
 * Builds the entity for a *new* expense. [existing] is null for a fresh create; when re-saving an
 * edit, pass the current entity so sync/version bookkeeping is preserved rather than reset.
 */
fun Expense.toEntity(existing: ExpenseEntity?, now: Long): ExpenseEntity = ExpenseEntity(
    localId = id,
    remoteId = existing?.remoteId,
    categoryLocalId = categoryId,
    amountMinorUnits = amount.amountMinorUnits,
    currencyCode = amount.currency.isoCode,
    description = description,
    notes = notes,
    date = date,
    receiptUri = receiptPath,
    createdAt = existing?.createdAt ?: now,
    updatedAt = now,
    // Any local create or edit invalidates whatever sync state existed before, regardless of
    // what it was (SYNCED, SYNC_FAILED, etc.) -- it now needs to go out again.
    syncStatus = SyncStatus.PENDING_UPLOAD,
    deletedAt = null,
    version = (existing?.version ?: 0) + 1,
)
