package org.dianqk.ruslin.ui.page.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.NotesRepository
import uniffi.ruslin.FfiAbbrNote
import uniffi.ruslin.FfiFolder
import javax.inject.Inject

data class NotesUiState(
    val items: List<FfiAbbrNote> = emptyList(),
    val folders: List<FfiFolder> = emptyList(),
    val selectedFolder: FfiFolder? = null,
    val isLoading: Boolean = false,
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
    }

    fun selectFolder(folder: FfiFolder?) {
        _uiState.update {
            it.copy(
                selectedFolder = folder
            )
        }
        loadAbbrNotes()
    }

    fun loadAbbrNotes() {
        viewModelScope.launch {
            notesRepository.loadAbbrNotes(uiState.value.selectedFolder?.id)
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

}
