package org.dianqk.ruslin.ui.page.settings.tools.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.NotesRepository
import uniffi.ruslin.FfiStatus
import javax.inject.Inject

data class DatabaseUiState(
    val status: String = ""
)

@HiltViewModel
class DatabaseStatusViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DatabaseUiState())
    val uiState: StateFlow<DatabaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notesRepository.readDatabaseStatus()
                .onSuccess {
                    updateStatus(it)
                }
        }
    }

    private fun updateStatus(status: FfiStatus) {
        val statusText = "Note: ${status.noteCount}\n" +
                "Folder: ${status.folderCount}\n" +
                "Resource: ${status.resourceCount}\n" +
                "Tag: ${status.tagCount}\n" +
                "NoteTag: ${status.noteTagCount}"
        _uiState.update {
            it.copy(status = statusText)
        }
    }
}