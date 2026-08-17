package com.mac.expensee.feature.expenses.domain.usecase

import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.expenses.domain.repository.ExpenseRepository
import com.mac.expensee.feature.expenses.domain.repository.ReceiptStorage

/**
 * Deletes the expense record and, if it had a receipt, the backing file too -- kept as one use
 * case (rather than leaving the repository to guess) since "does this delete cascade to a file"
 * is a business decision, not a persistence detail.
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val receiptStorage: ReceiptStorage,
) {
    suspend operator fun invoke(expenseId: String): AppResult<Unit> {
        val existing = expenseRepository.getExpenseById(expenseId)
        val result = expenseRepository.deleteExpense(expenseId)
        if (result is AppResult.Success) {
            (existing as? AppResult.Success)?.data?.receiptPath?.let { receiptStorage.deleteReceipt(it) }
        }
        return result
    }
}
