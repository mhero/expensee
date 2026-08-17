package com.mac.expensee.feature.expenses

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.common.sync.SyncStatus
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.feature.expenses.data.mapper.toDomain
import com.mac.expensee.feature.expenses.data.mapper.toEntity
import com.mac.expensee.feature.expenses.domain.model.Expense
import org.junit.Test

class ExpenseMappersTest {

    @Test
    fun `entity to domain drops sync metadata but preserves user-facing fields`() {
        val entity = ExpenseEntity(
            localId = "id-1",
            remoteId = "remote-1",
            categoryLocalId = "cat-1",
            amountMinorUnits = 1500,
            currencyCode = "USD",
            description = "Dinner",
            notes = "with friends",
            date = 1_000L,
            receiptUri = "/path/receipt.jpg",
            createdAt = 100L,
            updatedAt = 200L,
            syncStatus = SyncStatus.SYNCED,
            deletedAt = null,
            version = 3,
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("id-1")
        assertThat(domain.categoryId).isEqualTo("cat-1")
        assertThat(domain.amount).isEqualTo(Money(1500, CurrencyCode.USD))
        assertThat(domain.description).isEqualTo("Dinner")
        assertThat(domain.notes).isEqualTo("with friends")
        assertThat(domain.receiptPath).isEqualTo("/path/receipt.jpg")
    }

    @Test
    fun `editing an existing entity increments version and forces re-upload`() {
        val existing = ExpenseEntity(
            localId = "id-1",
            remoteId = "remote-1",
            categoryLocalId = "cat-1",
            amountMinorUnits = 1000,
            currencyCode = "USD",
            description = "Old",
            notes = null,
            date = 1_000L,
            receiptUri = null,
            createdAt = 100L,
            updatedAt = 100L,
            syncStatus = SyncStatus.SYNCED,
            deletedAt = null,
            version = 2,
        )
        val edited = Expense(
            id = "id-1",
            categoryId = "cat-1",
            amount = Money(2000, CurrencyCode.USD),
            description = "New",
            notes = null,
            date = 1_000L,
            receiptPath = null,
        )

        val entity = edited.toEntity(existing = existing, now = 500L)

        assertThat(entity.version).isEqualTo(3)
        assertThat(entity.syncStatus).isEqualTo(SyncStatus.PENDING_UPLOAD)
        assertThat(entity.remoteId).isEqualTo("remote-1")
        assertThat(entity.createdAt).isEqualTo(100L)
        assertThat(entity.updatedAt).isEqualTo(500L)
    }

    @Test
    fun `creating a new entity has no remoteId and version 1`() {
        val fresh = Expense(
            id = "id-2",
            categoryId = "cat-1",
            amount = Money(500, CurrencyCode.USD),
            description = "Coffee",
            notes = null,
            date = 1_000L,
            receiptPath = null,
        )

        val entity = fresh.toEntity(existing = null, now = 999L)

        assertThat(entity.remoteId).isNull()
        assertThat(entity.version).isEqualTo(1)
        assertThat(entity.createdAt).isEqualTo(999L)
        assertThat(entity.syncStatus).isEqualTo(SyncStatus.PENDING_UPLOAD)
    }
}
