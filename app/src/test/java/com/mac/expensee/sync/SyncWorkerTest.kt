package com.mac.expensee.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [SyncWorker] itself is thin -- almost all behavior lives in [SyncManager] (see
 * [SyncManagerTest]) -- so this only verifies the [ListenableWorker.Result] mapping, including
 * the retry-vs-give-up boundary at `MAX_RETRIES`. `TestListenableWorkerBuilder` needs a
 * [WorkerFactory] here because [SyncWorker]'s constructor takes a [SyncManager] dependency beyond
 * the (context, workerParams) pair WorkManager's default factory can satisfy via reflection --
 * the same role Koin's `worker {}` DSL plays at runtime (see `SyncModule`).
 */
@RunWith(RobolectricTestRunner::class)
class SyncWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun workerFactory(syncManager: SyncManager) = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker = SyncWorker(appContext, workerParameters, syncManager)
    }

    private fun buildWorker(syncManager: SyncManager, runAttemptCount: Int = 0): SyncWorker =
        TestListenableWorkerBuilder<SyncWorker>(context)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(workerFactory(syncManager))
            .build()

    @Test
    fun `doWork succeeds when sync is skipped`() = runTest {
        val syncManager = mockk<SyncManager> { coEvery { sync() } returns SyncManager.SyncOutcome.Skipped }

        val result = buildWorker(syncManager).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `doWork succeeds when sync succeeds`() = runTest {
        val syncManager = mockk<SyncManager> { coEvery { sync() } returns SyncManager.SyncOutcome.Success }

        val result = buildWorker(syncManager).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `doWork retries a failure while still under the retry limit`() = runTest {
        val syncManager = mockk<SyncManager> {
            coEvery { sync() } returns SyncManager.SyncOutcome.Failure(IllegalStateException("network blip"))
        }

        val result = buildWorker(syncManager, runAttemptCount = 4).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `doWork gives up once the retry limit is reached`() = runTest {
        val syncManager = mockk<SyncManager> {
            coEvery { sync() } returns SyncManager.SyncOutcome.Failure(IllegalStateException("still broken"))
        }

        val result = buildWorker(syncManager, runAttemptCount = 5).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }
}
