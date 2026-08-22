package com.mac.expensee.feature.expenses.presentation.addedit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import org.junit.Rule
import org.junit.Test

/**
 * Drives [AddEditExpenseScreen] directly with a fixed [AddEditExpenseUiState] rather than through
 * [AddEditExpenseRoute]/`koinViewModel()`, the same approach [feature.auth.presentation.login.LoginScreenTest]
 * uses for [feature.auth.presentation.login.LoginScreen]. The date picker and receipt/category
 * pickers are deliberately not exercised here -- they depend on `DatePickerDialog` and
 * `ExposedDropdownMenuBox` internals that are exactly the API surfaces the project README flags
 * as likely to need adjustment for the installed library versions; the text fields, error
 * display, and save button are stable, well-covered ground.
 *
 * The title and save button both read "Add expense" while creating a new expense, so this test
 * uses `screenTitle`/`saveButton` test tags rather than `onNodeWithText` wherever that ambiguity
 * would otherwise apply -- see those tags' usage sites in [AddEditExpenseScreen].
 */
class AddEditExpenseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: AddEditExpenseUiState = AddEditExpenseUiState(),
        onAction: (AddEditExpenseAction) -> Unit = {},
        onNavigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            AddEditExpenseScreen(state = state, onAction = onAction, onNavigateUp = onNavigateUp)
        }
    }

    @Test
    fun `the title reads Add expense for a new expense`() {
        setContent(state = AddEditExpenseUiState(expenseId = null))
        composeRule.onNodeWithTag("screenTitle").assertTextEquals("Add expense")
    }

    @Test
    fun `the title switches to Edit expense when an expenseId is present`() {
        setContent(state = AddEditExpenseUiState(expenseId = "e1"))
        composeRule.onNodeWithTag("screenTitle").assertTextEquals("Edit expense")
    }

    @Test
    fun `typing an amount reports AmountChanged with the full field value`() {
        val actions = mutableListOf<AddEditExpenseAction>()
        setContent(onAction = actions::add)

        composeRule.onNodeWithText("Amount").performTextInput("12.50")

        assertThat(actions.filterIsInstance<AddEditExpenseAction.AmountChanged>().last().value).isEqualTo("12.50")
    }

    @Test
    fun `typing a description reports DescriptionChanged with the full field value`() {
        val actions = mutableListOf<AddEditExpenseAction>()
        setContent(onAction = actions::add)

        composeRule.onNodeWithText("Description").performTextInput("Coffee")

        assertThat(actions.filterIsInstance<AddEditExpenseAction.DescriptionChanged>().last().value)
            .isEqualTo("Coffee")
    }

    @Test
    fun `a validation error message is shown when present in state`() {
        setContent(state = AddEditExpenseUiState(errorMessage = "Amount must be greater than zero"))

        composeRule.onNodeWithText("Amount must be greater than zero").assertIsDisplayed()
    }

    @Test
    fun `the selected category name is shown in the dropdown field`() {
        setContent(
            state = AddEditExpenseUiState(
                categories = listOf(ExpenseCategory(id = "c1", name = "Food & Dining", colorHex = "#EF6C00")),
                selectedCategoryId = "c1",
            ),
        )

        composeRule.onNodeWithText("Food & Dining").assertIsDisplayed()
    }

    @Test
    fun `the save button is disabled while saving`() {
        setContent(state = AddEditExpenseUiState(isSaving = true))

        composeRule.onNodeWithTag("saveButton").assertIsNotEnabled()
    }

    @Test
    fun `tapping save reports a Save action`() {
        val actions = mutableListOf<AddEditExpenseAction>()
        setContent(state = AddEditExpenseUiState(amountText = "5.00", description = "Coffee"), onAction = actions::add)

        composeRule.onNodeWithTag("saveButton").performClick()

        assertThat(actions).contains(AddEditExpenseAction.Save)
    }

    @Test
    fun `the save button label changes to Save changes when editing`() {
        setContent(state = AddEditExpenseUiState(expenseId = "e1"))

        composeRule.onNodeWithTag("saveButton").assertTextEquals("Save changes")
    }
}
