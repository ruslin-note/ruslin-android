package org.dianqk.ruslin.data

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import uniffi.ruslin.*
import javax.inject.Inject

class RuslinNotesRepository @Inject constructor(
    databaseDir: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val workManager: WorkManager,
    private val appContext: Context,
) : NotesRepository {
    private val data: RuslinAndroidData = RuslinAndroidData(databaseDir)

    override fun syncConfigExists(): Boolean = data.syncConfigExists()

    override suspend fun saveSyncConfig(config: SyncConfig): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.saveSyncConfig(config) }
        }

    override suspend fun getSyncConfig(): Result<SyncConfig?> = withContext(ioDispatcher) {
        kotlin.runCatching { data.getSyncConfig() }
    }

    override suspend fun sync(): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.sync() }
        }

    override suspend fun doSync(isOnStart: Boolean) {
        withContext(ioDispatcher) {
            workManager.cancelAllWork()
            val syncStrategy = appContext.dataStore.syncStrategy().first()
            if (isOnStart) {
                if (syncStrategy.syncOnStart) {
                    SyncWorker.enqueueOneTimeWork(workManager)
                }
            } else {
                SyncWorker.enqueueOneTimeWork(workManager)
            }
            if (syncStrategy.syncInterval > 0) {
                SyncWorker.enqueuePeriodicWork(
                    workManager = workManager,
                    syncInterval = syncStrategy.syncInterval,
                    syncOnlyWhenCharging = syncStrategy.syncOnlyWhenCharging,
                    syncOnlyOnWiFi = syncStrategy.syncOnlyWiFi,
                )
            }

        }

    }

    override fun newFolder(parentId: String?, title: String): FfiFolder =
        data.newFolder(parentId, title)

    override suspend fun replaceFolder(folder: FfiFolder): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.replaceFolder(folder) }
        }

    override suspend fun loadFolders(): Result<List<FfiFolder>> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.loadFolders() }
        }

    override suspend fun deleteFolder(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.deleteFolder(id) }
        }

    override suspend fun loadAbbrNotes(parentId: String?): Result<List<FfiAbbrNote>> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.loadAbbrNotes(parentId) }
        }

    override fun newNote(parentId: String?, title: String, body: String): FfiNote =
        data.newNote(parentId, title, body)

    override suspend fun loadNote(id: String): Result<FfiNote> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.loadNote(id) }
        }

    override suspend fun replaceNote(note: FfiNote): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.replaceNote(note) }
        }

    override suspend fun deleteNote(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.deleteNote(id) }
        }

    override suspend fun conflictNoteExists(): Result<Boolean> = withContext(ioDispatcher) {
        kotlin.runCatching { data.conflictNoteExists() }
    }

    override suspend fun loadAbbrConflictNotes(): Result<List<FfiAbbrNote>> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.loadAbbrConflictNotes() }
        }

}