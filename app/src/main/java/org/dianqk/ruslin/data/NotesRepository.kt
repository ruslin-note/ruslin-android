package org.dianqk.ruslin.data

import uniffi.ruslin.*

interface NotesRepository {

    fun syncConfigExists(): Boolean

    suspend fun saveSyncConfig(config: SyncConfig): Result<Unit>

    suspend fun sync(): Result<Unit>

    fun newFolder(parentId: String?, title: String): FfiFolder

    suspend fun replaceFolder(folder: FfiFolder): Result<Unit>

    suspend fun loadFolders(): Result<List<FfiFolder>>

    suspend fun deleteFolder(id: String): Result<Unit>

    suspend fun loadAbbrNotes(parentId: String?): Result<List<FfiAbbrNote>>

    fun newNote(parentId: String?, title: String, body: String): FfiNote

    suspend fun loadNote(id: String): Result<FfiNote>

    suspend fun replaceNote(note: FfiNote): Result<Unit>

    suspend fun deleteNote(id: String): Result<Unit>

}
