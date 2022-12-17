package org.dianqk.ruslin.ui.page.note_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.NotesRepository
import org.dianqk.ruslin.ui.RuslinDestinationsArgs
import uniffi.ruslin.FfiNote
import javax.inject.Inject

data class NoteDetailUiState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
)

const val TAG = "NoteDetailViewModel"

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val folderId: String? = savedStateHandle[RuslinDestinationsArgs.FOLDER_ID_ARG]
    private val noteId: String? = savedStateHandle[RuslinDestinationsArgs.NOTE_ID_ARG]
    private var note: FfiNote? = null
    private var edited: Boolean = false

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    fun saveNote() {
        if (noteId != null && note == null) {
            Log.w(TAG, "note note loaded yet")
            return
        }
        if (!edited) {
            return
        }
        val title: String
        if (uiState.value.title.isEmpty()) {
            title = uiState.value.body.split("\n").getOrNull(0) ?: ""
        } else {
            title = uiState.value.title
        }
        val body = uiState.value.body
        if (note != null) {
            note?.body = body
            note?.title = title
        } else {
            note = notesRepository.newNote(parentId = folderId, title = title, body = body)
        }
        viewModelScope.launch {
            notesRepository.replaceNote(note!!)
        }
    }

    fun updateTitle(newTitle: String) {
        edited = true
        _uiState.update {
            it.copy(title = newTitle)
        }
    }

    fun updateBody(newBody: String) {
        edited = true
        _uiState.update {
            it.copy(body = newBody)
        }
    }

    private fun loadNote(noteId: String) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            notesRepository.loadNote(noteId)
                .onSuccess { note ->
                    this@NoteDetailViewModel.note = note
                    _uiState.update {
                        it.copy(
                            title = note.title,
                            body = note.body,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    Log.e(TAG, "load note failed: $e")
                }
        }
    }

}