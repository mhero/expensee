package com.mac.expensee.feature.expenses.data.mapper

import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.core.network.dto.ExpenseDto
import com.mac.expensee.feature.expenses.domain.model.PendingExpenseUpload
import com.mac.expensee.feature.expenses.domain.model.RemoteExpenseSnapshot

fun ExpenseEntity.toPendingUpload(): PendingExpenseUpload = PendingExpenseUpload(
    localId = localId,
    remoteId = remoteId,
    categoryId = categoryLocalId,
    amount = Money(amountMinorUnits, CurrencyCode.fromIsoCode(currencyCode)),
    description = description,
    notes = notes,
    date = date,
    updatedAt = updatedAt,
    version = version,
)

/**
 * `id` is the server-facing identity: for a row never synced before, that's the client-generated
 * [PendingExpenseUpload.localId] itself (a "client supplies the id on create" contract); for a
 * previously-synced row, it's the server's own [PendingExpenseUpload.remoteId]. Either way,
 * `SyncManager` matches a pushed-back [ExpenseDto] to the upload that produced it by this same id.
 */
fun PendingExpenseUpload.toDto(): ExpenseDto = ExpenseDto(
    id = remoteId ?: localId,
    categoryId = categoryId,
    amountMinorUnits = amount.amountMinorUnits,
    currencyCode = amount.currency.isoCode,
    description = description,
    notes = notes,
    date = date,
    updatedAt = updatedAt,
    version = version,
)

fun ExpenseDto.toRemoteSnapshot(): RemoteExpenseSnapshot = RemoteExpenseSnapshot(
    remoteId = id,
    categoryId = categoryId,
    amount = Money(amountMinorUnits, CurrencyCode.fromIsoCode(currencyCode)),
    description = description,
    notes = notes,
    date = date,
    updatedAt = updatedAt,
    version = version,
)
