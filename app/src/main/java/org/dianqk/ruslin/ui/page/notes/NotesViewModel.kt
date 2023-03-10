package org.dianqk.ruslin.ui.page.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dianqk.ruslin.data.NotesRepository
import uniffi.ruslin.FfiAbbrNote
import uniffi.ruslin.FfiFolder
import uniffi.ruslin.FfiSyncInfo
import javax.inject.Inject

data class Folder(
    val ffiFolder: FfiFolder,
    val subFolders: MutableList<Folder> = mutableListOf(),
    private var _isExpanded: MutableStateFlow<Boolean> = MutableStateFlow(false), // Whether the internal update of data is reasonable ?
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()
) {
    fun setExpanded(isExpanded: Boolean) {

        _isExpanded.update { isExpanded }
    }
}

data class NotesUiState(
    val items: List<FfiAbbrNote>? = null,
    val folders: List<Folder> = emptyList(),
    val selectedFolder: FfiFolder? = null,
    val isLoading: Boolean = false,
    val conflictNoteExists: Boolean = false,
    val showConflictNotes: Boolean = false,
    val isSyncing: Boolean = false,
    val syncResult: Result<FfiSyncInfo>? = null,
)

const val TAG = "NotesViewModel"

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
        loadAbbrNotes()
        checkConflictNoteExists()
        viewModelScope.launch {
            notesRepository.syncFinished.collect { syncResult ->
                _uiState.update {
                    it.copy(syncResult = syncResult)
                }
                syncResult
                    .onSuccess { syncInfo ->
                        if (syncInfo.conflictNoteCount > 0
                            || syncInfo.otherConflictCount > 0
                            || syncInfo.pullCount > 0
                            || syncInfo.deleteCount > 0
                        ) {
                            reloadAllAfterSync()
                        }
                    }
                    .onFailure { e ->
                        Log.d(TAG, "sync failed: $e")
                    }
            }
        }
        viewModelScope.launch {
            notesRepository.isSyncing.collect { isSyncing ->
                _uiState.update {
                    if (isSyncing) {
                        it.copy(isSyncing = true, syncResult = null)
                    } else {
                        it.copy(isSyncing = false)
                    }
                }
            }
        }
        viewModelScope.launch {
            notesRepository.notesChangedManually.collect {
                loadAbbrNotes()
            }
        }
    }

    fun selectFolder(folder: FfiFolder?) {
        _uiState.update {
            it.copy(
                selectedFolder = folder,
                showConflictNotes = false
            )
        }
        loadAbbrNotes()
    }

    fun showConflictNotes() {
        _uiState.update {
            it.copy(
                selectedFolder = null,
                showConflictNotes = true
            )
        }
        loadAbbrNotes()
    }

    fun syncConfigExists(): Boolean = notesRepository.syncConfigExists()

    fun sync() {
        viewModelScope.launch {
            notesRepository.doSync(isOnStart = false, fromScratch = false)
        }
    }

    fun dismissSyncErrorMessage() {
        _uiState.update {
            it.copy(syncResult = null)
        }
    }

    private fun reloadAllAfterSync() {
        loadAbbrNotes()
        loadFolders()
        checkConflictNoteExists()
    }

    private fun loadAbbrNotes() {
        val showConflictNotes = uiState.value.showConflictNotes
        _uiState.update {
            it.copy(
                isLoading = true,
            )
        }
        viewModelScope.launch {
            if (showConflictNotes) {
                notesRepository.loadAbbrConflictNotes()
            } else {
                notesRepository.loadAbbrNotes(uiState.value.selectedFolder?.id)
            }
                .onSuccess { notes ->
                    _uiState.update {
                        it.copy(
                            items = notes,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    Log.e(TAG, "load abbr notes failed: $e")
                }
        }
    }

    fun changeFolder(folder: FfiFolder) {
        viewModelScope.launch {
            notesRepository.replaceFolder(folder)
            loadFoldersFromRepo()
        }
    }

    suspend fun deleteFolder(folder: FfiFolder) {
        notesRepository.deleteFolder(folder.id)
        loadFoldersFromRepo()
    }

    fun createFolder(title: String) {
        viewModelScope.launch {
            val folder = notesRepository.newFolder(parentId = null, title = title)
            notesRepository.replaceFolder(folder)
            loadFoldersFromRepo()
        }
    }

    private suspend fun loadFoldersFromRepo() {
        notesRepository.loadFolders()
            .onSuccess { folders ->
                withContext(Dispatchers.Default) {
                    updateFoldersFromFfi(folders)
                }
                withContext(Dispatchers.Default) {
                    if (folders.find { it.id == _uiState.value.selectedFolder?.id } == null) {
                        selectFolder(null)
                    }
                }
            }
            .onFailure { e ->
                Log.e(TAG, "load folders failed: $e")
            }
    }

    private fun updateFoldersFromFfi(ffiFolders: List<FfiFolder>) {
        val allFolders = ffiFolders.map { Folder(it) }
        val folderMap = mutableMapOf<String, Folder>()
        allFolders.forEach { folder ->
            folderMap[folder.ffiFolder.id] = folder
        }
        allFolders.forEach { folder ->
            folder.ffiFolder.parentId?.let {
                folderMap[it]?.subFolders?.add(folder)
            }
        }
        val rootFolders = allFolders.filter { it.ffiFolder.parentId == null }
        _uiState.update {
            it.copy(folders = rootFolders)
        }
    }

    private fun loadFolders() {
        viewModelScope.launch {
            loadFoldersFromRepo()
        }
    }

    private fun checkConflictNoteExists() {
        viewModelScope.launch {
            notesRepository.conflictNoteExists()
                .onSuccess { exists ->
                    _uiState.update {
                        it.copy(conflictNoteExists = exists)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "check conflict note failed: $e")
                }
        }
    }

    suspend fun deleteNotes(ids: List<String>) = notesRepository.deleteNotes(ids)
}
