package com.mac.expensee.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies [CleanupWorker] purges tombstones on both DAOs and reports success -- see its KDoc for
 * why that makes it a safe no-op today (nothing reaches `SyncStatus.SYNCED` without a backend).
 */
@RunWith(RobolectricTestRunner::class)
class CleanupWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val expenseDao: ExpenseDao = mockk(relaxed = true)
    private val categoryDao: CategoryDao = mockk(relaxed = true)

    private fun buildWorker(): CleanupWorker =
        TestListenableWorkerBuilder<CleanupWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker = CleanupWorker(appContext, workerParameters, expenseDao, categoryDao)
                },
            )
            .build()

    @Test
    fun `doWork purges tombstones on both DAOs and succeeds`() = runTest {
        val result = buildWorker().doWork()

        coVerify { expenseDao.purgeSyncedTombstones() }
        coVerify { categoryDao.purgeSyncedTombstones() }
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
