package com.mac.expensee.feature.expenses.domain.validation

import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.feature.expenses.domain.model.Expense

private const val MAX_DESCRIPTION_LENGTH = 140
private const val MAX_NOTES_LENGTH = 1000

/** Pure business rules for what makes an [Expense] valid, independent of Room/Compose. */
object ExpenseValidator {

    fun validate(expense: Expense): DataError.Validation.InvalidField? {
        if (expense.amount.amountMinorUnits <= 0) {
            return DataError.Validation.InvalidField("amount", "Enter an amount greater than zero")
        }
        if (expense.description.isBlank()) {
            return DataError.Validation.InvalidField("description", "Description is required")
        }
        if (expense.description.length > MAX_DESCRIPTION_LENGTH) {
            return DataError.Validation.InvalidField("description", "Description is too long")
        }
        if (expense.categoryId.isBlank()) {
            return DataError.Validation.InvalidField("category", "Choose a category")
        }
        if ((expense.notes?.length ?: 0) > MAX_NOTES_LENGTH) {
            return DataError.Validation.InvalidField("notes", "Notes are too long")
        }
        return null
    }
}
