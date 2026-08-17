package com.mac.expensee.feature.expenses

import android.net.Uri
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.feature.expenses.domain.repository.ReceiptStorage

class FakeReceiptStorage : ReceiptStorage {
    val deletedPaths = mutableListOf<String>()

    override suspend fun saveReceipt(sourceUri: Uri): AppResult<String> = "fake/path.jpg".asSuccess()

    override suspend fun deleteReceipt(storedPath: String) {
        deletedPaths += storedPath
    }

    override fun exists(storedPath: String): Boolean = true
}
