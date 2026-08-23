package com.mac.expensee.feature.expenses

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.common.money.CurrencyCode
import com.mac.expensee.core.common.money.Money
import com.mac.expensee.core.testing.MainDispatcherRule
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.expenses.domain.model.Expense
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import com.mac.expensee.feature.expenses.domain.repository.ExpenseCategoryLookupRepository
import com.mac.expensee.feature.expenses.domain.usecase.DeleteExpenseUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpenseCategoriesUseCase
import com.mac.expensee.feature.expenses.domain.usecase.ObserveExpensesUseCase
import com.mac.expensee.feature.expenses.presentation.list.ExpensesListAction
import com.mac.expensee.feature.expenses.presentation.list.ExpensesListViewModel
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

class ExpensesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expenseRepository = FakeExpenseRepository()
    private val categoryLookupRepository = FakeCategoryLookupRepository()
    private val receiptStorage = FakeReceiptStorage()

    private fun buildViewModel() = ExpensesListViewModel(
        observeExpensesUseCase = ObserveExpensesUseCase(expenseRepository),
        observeExpenseCategoriesUseCase = ObserveExpenseCategoriesUseCase(categoryLookupRepository),
        deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository, receiptStorage),
    )

    @Test
    fun `starts empty then reflects an added expense with resolved category name`() = runTest {
        val viewModel = buildViewModel()

        expenseRepository.addExpense(
            Expense(
                id = "e1",
                categoryId = "cat-1",
                amount = Money(1200, CurrencyCode.USD),
                description = "Groceries",
                notes = null,
                date = System.currentTimeMillis(),
                receiptPath = null,
            ),
        )

        viewModel.state.test {
            val state = awaitItem()
            val content = state.items as UiState.Content
            assertThat(content.data).hasSize(1)
            assertThat(content.data.first().categoryName).isEqualTo("Food")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting an expense removes it from the list`() = runTest {
        val viewModel = buildViewModel()
        expenseRepository.addExpense(
            Expense("e1", "cat-1", Money(500, CurrencyCode.USD), "Coffee", null, System.currentTimeMillis(), null),
        )

        viewModel.onAction(ExpensesListAction.DeleteExpense("e1"))

        viewModel.state.test {
            val content = awaitItem().items as UiState.Content
            assertThat(content.data).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `total sums expenses when they share one currency`() = runTest {
        val viewModel = buildViewModel()
        expenseRepository.addExpense(
            Expense("e1", "cat-1", Money(1000, CurrencyCode.USD), "Lunch", null, System.currentTimeMillis(), null),
        )
        expenseRepository.addExpense(
            Expense("e2", "cat-1", Money(500, CurrencyCode.USD), "Coffee", null, System.currentTimeMillis(), null),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.total).isEqualTo(Money(1500, CurrencyCode.USD))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `total is null (not a crash) when expenses span more than one currency`() = runTest {
        // Money.sum throws IllegalArgumentException on mismatched currencies (see Money.plus) --
        // this guards against that once expenses can legitimately be created in different
        // currencies (see AddEditExpenseViewModel).
        val viewModel = buildViewModel()
        expenseRepository.addExpense(
            Expense("e1", "cat-1", Money(1000, CurrencyCode.USD), "Lunch", null, System.currentTimeMillis(), null),
        )
        expenseRepository.addExpense(
            Expense("e2", "cat-1", Money(900, CurrencyCode.EUR), "Dinner", null, System.currentTimeMillis(), null),
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.total).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `total is null when there are no expenses`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.total).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
