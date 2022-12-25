package org.dianqk.ruslin.ui.page.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.NotesRepository
import org.dianqk.ruslin.data.SyncWorker
import uniffi.ruslin.FfiAbbrNote
import uniffi.ruslin.FfiFolder
import javax.inject.Inject

data class NotesUiState(
    val items: List<FfiAbbrNote> = emptyList(),
    val folders: List<FfiFolder> = emptyList(),
    val selectedFolder: FfiFolder? = null,
    val isLoading: Boolean = false,
    val selectedNote: FfiAbbrNote? = null,
    val conflictNoteExists: Boolean = false,
    val showConflictNotes: Boolean = false,
)

const val TAG = "NotesViewModel"

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    workManager: WorkManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfosByTagLiveData(SyncWorker.WORK_NAME)

    init {
        loadFolders()
        checkConflictNoteExists()
    }

    fun selectFolder(folder: FfiFolder?) {
        _uiState.update {
            it.copy(
                selectedFolder = folder,
                showConflictNotes = false,
            )
        }
        loadAbbrNotes()
    }

    fun showConflictNotes() {
        _uiState.update {
            it.copy(
                selectedFolder = null,
                showConflictNotes = true,
            )
        }
        loadAbbrNotes()
    }

    fun selectNote(note: FfiAbbrNote) {
        _uiState.update {
            it.copy(selectedNote = note)
        }
    }

    fun unselecteNote() {
        _uiState.update {
            it.copy(selectedNote = null)
        }
    }

    fun syncConfigExists(): Boolean = notesRepository.syncConfigExists()

    fun sync() {
        viewModelScope.launch {
            notesRepository.doSync(isOnStart = false)
        }
    }

    fun reloadAllAfterSync() {
        loadAbbrNotes()
        loadFolders()
        checkConflictNoteExists()
    }

    fun loadAbbrNotes() {
        val showConflictNotes = uiState.value.showConflictNotes
        _uiState.update {
            it.copy(
                isLoading = true
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

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            notesRepository.deleteNote(noteId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            items = it.items.filter { note -> note.id != noteId }
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "delete note failed: $e")
                }
        }
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
                _uiState.update {
                    it.copy(folders = folders)
                }
            }
            .onFailure { e ->
                Log.e(TAG, "load folders failed: $e")
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

}
