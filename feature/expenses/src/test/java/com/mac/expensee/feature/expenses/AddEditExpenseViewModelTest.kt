package com.mac.expensee.feature.expenses

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.testing.MainDispatcherRule
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import com.mac.expensee.feature.expenses.domain.repository.DefaultCurrencyRepository
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import com.mac.expensee.feature.expenses.domain.usecase.AddExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.GetDefaultCurrencyUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseCategoriesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.SaveReceiptUseCase
import com.mac.expensee.feature.expenses.domain.usecase.UpdateExpenseUseCase
import com.mac.expensee.feature.expenses.presentation.addedit.AddEditExpenseAction
import com.mac.expensee.feature.expenses.presentation.addedit.AddEditExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private class FakeCategoryLookupRepository : ExpenseCategoryLookupRepository {
    val categories = MutableStateFlow(
        listOf(ExpenseCategory(id = "cat-1", name = "Food", colorHex = "#EF6C00")),
    )
    override fun observeCategories() = categories
    override suspend fun getCategoryById(id: String) = categories.value.firstOrNull { it.id == id }
}

private class FakeDefaultCurrencyRepository(var currency: CurrencyCode = CurrencyCode.USD) : DefaultCurrencyRepository {
    override suspend fun getDefaultCurrency(): CurrencyCode = currency
}

/**
 * Covers the bug this was written to fix: a new expense used to always save in USD regardless of
 * the user's currency setting, and editing an existing expense silently reset it to USD on save.
 * See [AddEditExpenseViewModel]'s KDoc and [com.mac.expensee.feature.expenses.presentation.addedit.AddEditExpenseUiState.currency].
 */
class AddEditExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expenseRepository = FakeExpenseRepository()
    private val categoryLookupRepository = FakeCategoryLookupRepository()
    private val receiptStorage = FakeReceiptStorage()
    private val defaultCurrencyRepository = FakeDefaultCurrencyRepository()

    private fun buildViewModel(existingExpenseId: String? = null) = AddEditExpenseViewModel(
        existingExpenseId = existingExpenseId,
        addExpenseUseCase = AddExpenseUseCase(expenseRepository),
        updateExpenseUseCase = UpdateExpenseUseCase(expenseRepository),
        observeExpenseUseCase = ObserveExpenseUseCase(expenseRepository),
        observeExpenseCategoriesUseCase = ObserveExpenseCategoriesUseCase(categoryLookupRepository),
        saveReceiptUseCase = SaveReceiptUseCase(receiptStorage),
        getDefaultCurrencyUseCase = GetDefaultCurrencyUseCase(defaultCurrencyRepository),
    )

    @Test
    fun `a new expense defaults to the user's current default-currency setting`() = runTest {
        defaultCurrencyRepository.currency = CurrencyCode.EUR

        val viewModel = buildViewModel(existingExpenseId = null)

        viewModel.state.test {
            var state = awaitItem()
            while (state.currency != CurrencyCode.EUR) state = awaitItem()
            assertThat(state.currency).isEqualTo(CurrencyCode.EUR)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving a new expense uses the resolved default currency, not a hardcoded one`() = runTest {
        defaultCurrencyRepository.currency = CurrencyCode.GBP
        val viewModel = buildViewModel(existingExpenseId = null)

        viewModel.state.test {
            var state = awaitItem()
            while (state.currency != CurrencyCode.GBP) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(AddEditExpenseAction.AmountChanged("12.00"))
        viewModel.onAction(AddEditExpenseAction.DescriptionChanged("Taxi"))
        viewModel.onAction(AddEditExpenseAction.CategorySelected("cat-1"))
        viewModel.onAction(AddEditExpenseAction.Save)

        val saved = expenseRepository.currentExpenses().single()
        assertThat(saved.amount.currency).isEqualTo(CurrencyCode.GBP)
    }

    @Test
    fun `editing an existing expense keeps its own currency, ignoring the current default setting`() = runTest {
        expenseRepository.addExpense(
            Expense(
                id = "e1",
                categoryId = "cat-1",
                amount = Money(500, CurrencyCode.JPY),
                description = "Ramen",
                notes = null,
                date = System.currentTimeMillis(),
                receiptPath = null,
            ),
        )
        // The default-currency setting has since changed to EUR -- editing e1 must not follow it.
        defaultCurrencyRepository.currency = CurrencyCode.EUR

        val viewModel = buildViewModel(existingExpenseId = "e1")

        viewModel.state.test {
            var state = awaitItem()
            while (state.isLoadingExisting) state = awaitItem()
            assertThat(state.currency).isEqualTo(CurrencyCode.JPY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving an edited expense preserves its original currency`() = runTest {
        expenseRepository.addExpense(
            Expense(
                id = "e1",
                categoryId = "cat-1",
                amount = Money(500, CurrencyCode.JPY),
                description = "Ramen",
                notes = null,
                date = System.currentTimeMillis(),
                receiptPath = null,
            ),
        )
        defaultCurrencyRepository.currency = CurrencyCode.EUR
        val viewModel = buildViewModel(existingExpenseId = "e1")

        viewModel.state.test {
            var state = awaitItem()
            while (state.isLoadingExisting) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(AddEditExpenseAction.AmountChanged("600"))
        viewModel.onAction(AddEditExpenseAction.Save)

        val saved = expenseRepository.currentExpenses().single { it.id == "e1" }
        assertThat(saved.amount.currency).isEqualTo(CurrencyCode.JPY)
    }
}
