package com.mac.expensee.feature.expenses

import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.validation.ExpenseValidator
import org.junit.Test

class ExpenseValidatorTest {

    private fun validExpense() = Expense(
        id = "1",
        categoryId = "cat-1",
        amount = Money(1000, CurrencyCode.USD),
        description = "Coffee",
        notes = null,
        date = System.currentTimeMillis(),
        receiptPath = null,
    )

    @Test
    fun `valid expense passes`() {
        assertThat(ExpenseValidator.validate(validExpense())).isNull()
    }

    @Test
    fun `zero amount is invalid`() {
        val expense = validExpense().copy(amount = Money(0, CurrencyCode.USD))
        assertThat(ExpenseValidator.validate(expense)).isNotNull()
    }

    @Test
    fun `blank description is invalid`() {
        val expense = validExpense().copy(description = "  ")
        assertThat(ExpenseValidator.validate(expense)).isNotNull()
    }

    @Test
    fun `blank category is invalid`() {
        val expense = validExpense().copy(categoryId = "")
        assertThat(ExpenseValidator.validate(expense)).isNotNull()
    }

    @Test
    fun `overly long description is invalid`() {
        val expense = validExpense().copy(description = "x".repeat(200))
        assertThat(ExpenseValidator.validate(expense)).isNotNull()
    }
}
