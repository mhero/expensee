package com.mac.expensee.feature.expenses.domain.repository

import android.net.Uri
import com.mac.expensee.core.common.result.AppResult

/**
 * Abstraction around where receipt images physically live. Today this is always
 * `LocalReceiptStorage` (app-private internal storage); a future `RemoteReceiptStorage` could
 * upload to object storage and return a remote reference instead, without `AddEditExpenseViewModel`
 * changing at all.
 */
interface ReceiptStorage {
    /**
     * Copies the picker-provided [sourceUri] into storage this class owns and returns a stable
     * local reference to it. Must copy rather than retain [sourceUri] itself: the photo picker's
     * grant on that Uri is not guaranteed to outlive this call.
     */
    suspend fun saveReceipt(sourceUri: Uri): AppResult<String>

    suspend fun deleteReceipt(storedPath: String)

    /** True if the file a stored path points to is still present (it may have been cleared by the OS). */
    fun exists(storedPath: String): Boolean
}
