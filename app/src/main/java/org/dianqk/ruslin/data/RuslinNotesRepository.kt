package org.dianqk.ruslin.data

import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import uniffi.ruslin.*
import java.io.File
import javax.inject.Inject

class RuslinNotesRepository @Inject constructor(
    databaseDir: String,
    override val resourceDir: File,
    private val logTxtFile: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val workManager: WorkManager,
    private val appContext: Context,
    private val applicationScope: CoroutineScope
) : NotesRepository {
    private val _isSyncing = MutableSharedFlow<Boolean>(replay = 0)
    override val isSyncing: SharedFlow<Boolean> = _isSyncing.asSharedFlow()

    private val _syncFinished = MutableSharedFlow<Result<FfiSyncInfo>>(replay = 0)
    override val syncFinished: SharedFlow<Result<FfiSyncInfo>> = _syncFinished.asSharedFlow()

    private val _notesChangedManually = MutableSharedFlow<Unit>(replay = 0)
    override val notesChangedManually: SharedFlow<Unit> = _notesChangedManually.asSharedFlow()

    private val data: RuslinAndroidData =
        RuslinAndroidData(databaseDir, resourceDir.absolutePath, logTxtFile.absolutePath)

    override fun syncConfigExists(): Boolean = data.syncConfigExists()

    override suspend fun saveSyncConfig(config: SyncConfig): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.saveSyncConfig(config) }
        }

    override suspend fun getSyncConfig(): Result<SyncConfig?> = withContext(ioDispatcher) {
        kotlin.runCatching { data.getSyncConfig() }
    }

    override suspend fun synchronize(fromScratch: Boolean): Result<FfiSyncInfo> =
        withContext(ioDispatcher) {
            _isSyncing.emit(true)
            val syncResult = kotlin.runCatching { data.synchronize(fromScratch = fromScratch) }
            _isSyncing.emit(false)
            _syncFinished.emit(syncResult)
            syncResult
        }

    override fun doSync(isOnStart: Boolean, fromScratch: Boolean) {
        applicationScope.launch {
            workManager.cancelAllWork()
            val syncStrategy = appContext.dataStore.syncStrategy().first()
            if (isOnStart) {
                if (syncStrategy.syncOnStart) {
                    SyncWorker.enqueueOneTimeWork(workManager, fromScratch = fromScratch)
                }
            } else {
                SyncWorker.enqueueOneTimeWork(workManager, fromScratch = fromScratch)
            }
            if (syncStrategy.syncInterval > 0) {
                SyncWorker.enqueuePeriodicWork(
                    workManager = workManager,
                    syncInterval = syncStrategy.syncInterval,
                    syncOnlyWhenCharging = syncStrategy.syncOnlyWhenCharging,
                    syncOnlyOnWiFi = syncStrategy.syncOnlyWiFi
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
                .onSuccess { _notesChangedManually.emit(Unit) }
        }

    override suspend fun deleteNote(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.deleteNote(id) }
                .onSuccess { _notesChangedManually.emit(Unit) }
        }

    override suspend fun conflictNoteExists(): Result<Boolean> = withContext(ioDispatcher) {
        kotlin.runCatching { data.conflictNoteExists() }
    }

    override suspend fun loadAbbrConflictNotes(): Result<List<FfiAbbrNote>> =
        withContext(ioDispatcher) {
            kotlin.runCatching { data.loadAbbrConflictNotes() }
        }

    override suspend fun readLog(): String = withContext(ioDispatcher) {
        if (logTxtFile.exists()) {
            logTxtFile.readText()
        } else {
            ""
        }
    }

    override suspend fun readDatabaseStatus(): Result<FfiStatus> = withContext(ioDispatcher) {
        kotlin.runCatching { data.databaseStatus() }
    }

    override suspend fun search(
        searchTerm: String
    ): Result<List<FfiSearchNote>> = withContext(ioDispatcher) {
        kotlin.runCatching {
            data.search(
                searchTerm = searchTerm,
            )
        }
    }

    override fun createResource(
        title: String,
        mime: String,
        fileExtension: String,
        size: Int
    ): FfiResource =
        data.createResource(title = title, mime = mime, fileExtension = fileExtension, size = size)

    override suspend fun saveResource(resource: FfiResource): Result<Unit> =
        withContext(ioDispatcher) {
            kotlin.runCatching {
                data.saveResource(resource)
            }
        }

    override fun loadResource(id: String): FfiResource = data.loadResource(id = id)

}
