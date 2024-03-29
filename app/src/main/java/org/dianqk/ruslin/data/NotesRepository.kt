package org.dianqk.ruslin.data

import kotlinx.coroutines.flow.SharedFlow
import uniffi.ruslin.FfiAbbrNote
import uniffi.ruslin.FfiFolder
import uniffi.ruslin.FfiNote
import uniffi.ruslin.FfiResource
import uniffi.ruslin.FfiSearchNote
import uniffi.ruslin.FfiStatus
import uniffi.ruslin.FfiSyncInfo
import uniffi.ruslin.SyncConfig
import java.io.File

interface NotesRepository {

    fun syncConfigExists(): Boolean

    suspend fun saveSyncConfig(config: SyncConfig): Result<Unit>

    suspend fun getSyncConfig(): Result<SyncConfig?>

    suspend fun synchronize(fromScratch: Boolean): Result<FfiSyncInfo>

    val isSyncing: SharedFlow<Boolean>
    val syncFinished: SharedFlow<Result<FfiSyncInfo>>

    val notesChangedManually: SharedFlow<Unit>

    val resourceDir: File

    fun doSync(isOnStart: Boolean, fromScratch: Boolean)

    fun newFolder(parentId: String?, title: String): FfiFolder

    suspend fun replaceFolder(folder: FfiFolder): Result<Unit>

    suspend fun loadFolders(): Result<List<FfiFolder>>

    suspend fun deleteFolder(id: String): Result<Unit>

    suspend fun loadAbbrNotes(parentId: String?): Result<List<FfiAbbrNote>>

    fun newNote(parentId: String?, title: String, body: String): FfiNote

    suspend fun loadNote(id: String): Result<FfiNote>

    suspend fun replaceNote(note: FfiNote): Result<Unit>

    suspend fun deleteNote(id: String): Result<Unit>

    suspend fun deleteNotes(ids: List<String>): Result<Unit>

    suspend fun conflictNoteExists(): Result<Boolean>

    suspend fun loadAbbrConflictNotes(): Result<List<FfiAbbrNote>>

    suspend fun readLog(): String

    suspend fun readDatabaseStatus(): Result<FfiStatus>

    suspend fun search(searchTerm: String): Result<List<FfiSearchNote>>

    fun createResource(title: String, mime: String, fileExtension: String, size: Int): FfiResource

    suspend fun saveResource(resource: FfiResource): Result<Unit>

    fun loadResource(id: String): Result<FfiResource>

    suspend fun parseMarkdownToPreviewHtml(text: String): String

    suspend fun prepareJieba(): Result<Unit>

}
