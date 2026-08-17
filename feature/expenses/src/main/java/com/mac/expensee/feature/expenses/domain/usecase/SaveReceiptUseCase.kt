package com.mac.expensee.feature.expenses.domain.usecase

import android.net.Uri
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.feature.expenses.domain.repository.ReceiptStorage

class SaveReceiptUseCase(private val receiptStorage: ReceiptStorage) {
    suspend operator fun invoke(sourceUri: Uri): AppResult<String> = receiptStorage.saveReceipt(sourceUri)
}
