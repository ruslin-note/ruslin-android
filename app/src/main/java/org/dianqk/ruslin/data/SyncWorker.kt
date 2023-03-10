package org.dianqk.ruslin.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notesRepository: NotesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val fromScratch = inputData.getBoolean(FROM_SCRATCH, false)
        val syncResult = notesRepository.synchronize(fromScratch = fromScratch)
        return@withContext if (syncResult.isSuccess) Result.success() else Result.failure()
    }

    companion object {
        private const val FROM_SCRATCH = "fromScratch"
        private const val WORK_NAME = "Ruslin"

        fun enqueueOneTimeWork(workerManager: WorkManager, fromScratch: Boolean) {
            workerManager.enqueue(
                OneTimeWorkRequestBuilder<SyncWorker>().addTag(WORK_NAME)
                    .setInputData(workDataOf(FROM_SCRATCH to fromScratch)).build()
            )
        }

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: Long,
            syncOnlyWhenCharging: Boolean,
            syncOnlyOnWiFi: Boolean
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<SyncWorker>(syncInterval, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(syncOnlyWhenCharging)
                            .setRequiredNetworkType(
                                if (syncOnlyOnWiFi) NetworkType.UNMETERED else NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .addTag(WORK_NAME)
                    .setInputData(workDataOf(FROM_SCRATCH to false))
                    .setInitialDelay(15, TimeUnit.MINUTES)
                    .build()
            )
        }
    }
}
