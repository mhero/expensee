package com.mac.expensee.feature.expenses

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.usecase.DeleteExpenseUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteExpenseUseCaseTest {

    private val repository = FakeExpenseRepository()
    private val receiptStorage = FakeReceiptStorage()
    private val useCase = DeleteExpenseUseCase(repository, receiptStorage)

    private fun expenseWithReceipt() = Expense(
        id = "e1",
        categoryId = "cat-1",
        amount = Money(500, CurrencyCode.USD),
        description = "Taxi",
        notes = null,
        date = System.currentTimeMillis(),
        receiptPath = "some/receipt.jpg",
    )

    @Test
    fun `deleting an expense with a receipt also deletes the receipt file`() = runTest {
        repository.addExpense(expenseWithReceipt())

        val result = useCase("e1")

        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat(receiptStorage.deletedPaths).containsExactly("some/receipt.jpg")
        assertThat(repository.currentExpenses()).isEmpty()
    }

    @Test
    fun `deleting an expense without a receipt does not touch receipt storage`() = runTest {
        repository.addExpense(expenseWithReceipt().copy(receiptPath = null))

        useCase("e1")

        assertThat(receiptStorage.deletedPaths).isEmpty()
    }

    @Test
    fun `deleting a nonexistent expense fails without touching receipt storage`() = runTest {
        val result = useCase("ghost")

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat(receiptStorage.deletedPaths).isEmpty()
    }
}
