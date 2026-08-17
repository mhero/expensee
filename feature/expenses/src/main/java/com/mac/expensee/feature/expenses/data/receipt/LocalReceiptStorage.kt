package com.mac.expensee.feature.expenses.data.receipt

import android.content.Context
import android.net.Uri
import com.mac.expensee.core.common.dispatcher.DispatcherProvider
import com.mac.expensee.core.common.result.AppResult
import com.mac.expensee.core.common.result.DataError
import com.mac.expensee.core.common.result.asError
import com.mac.expensee.core.common.result.asSuccess
import com.mac.expensee.feature.expenses.domain.repository.ReceiptStorage
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.withContext

/**
 * Stores receipts as plain files under app-private internal storage (`filesDir/receipts/`) --
 * never in Room (see project README security model: this is "plaintext at rest", same as any
 * other app-sandboxed file, not separately encrypted).
 *
 * Deliberately copies bytes out of [sourceUri] immediately rather than persisting the picker Uri:
 * the modern photo picker (`ActivityResultContracts.PickVisualMedia`) only grants transient read
 * access, so holding onto the Uri itself (instead of a local copy) would silently break receipt
 * display after the app process restarts.
 */
class LocalReceiptStorage(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : ReceiptStorage {

    private val receiptsDir: File
        get() = File(context.filesDir, RECEIPTS_DIR_NAME).apply { mkdirs() }

    override suspend fun saveReceipt(sourceUri: Uri): AppResult<String> = withContext(dispatcherProvider.io) {
        try {
            val destination = File(receiptsDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext DataError.Local.Unknown("Could not open the selected image").asError()
            destination.absolutePath.asSuccess()
        } catch (e: IOException) {
            DataError.Local.Unknown(e.message).asError()
        } catch (e: SecurityException) {
            DataError.Local.Unknown(e.message).asError()
        }
    }

    override suspend fun deleteReceipt(storedPath: String) = withContext(dispatcherProvider.io) {
        runCatching { File(storedPath).takeIf { it.exists() }?.delete() }
        Unit
    }

    override fun exists(storedPath: String): Boolean = File(storedPath).exists()

    private companion object {
        const val RECEIPTS_DIR_NAME = "receipts"
    }
}
