package com.mac.expensee.feature.expenses

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.usecase.AddExpenseUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddExpenseUseCaseTest {

    private val repository = FakeExpenseRepository()
    private val useCase = AddExpenseUseCase(repository)

    private fun expense(amountMinorUnits: Long = 500, description: String = "Lunch") = Expense(
        id = "e1",
        categoryId = "cat-1",
        amount = Money(amountMinorUnits, CurrencyCode.USD),
        description = description,
        notes = null,
        date = System.currentTimeMillis(),
        receiptPath = null,
    )

    @Test
    fun `valid expense is persisted`() = runTest {
        val result = useCase(expense())
        assertThat(result).isInstanceOf(AppResult.Success::class.java)
        assertThat(repository.currentExpenses()).hasSize(1)
    }

    @Test
    fun `invalid expense is rejected before reaching the repository`() = runTest {
        val result = useCase(expense(amountMinorUnits = 0))
        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat(repository.currentExpenses()).isEmpty()
    }
}
