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
import javax.inject.Inject

data class DatabaseUiState(
    val status: String = "",
    val isSyncing: Boolean = false,
    val syncResult: Result<String>? = null,
)

@HiltViewModel
class DatabaseStatusViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DatabaseUiState())
    val uiState: StateFlow<DatabaseUiState> = _uiState.asStateFlow()

    init {
        updateStatus()
        viewModelScope.launch {
            notesRepository.syncFinished.collect { syncResult ->
                syncResult.onSuccess { syncInfo ->
                    _uiState.update {
                        it.copy(
                            syncResult = Result.success("$syncInfo")
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            syncResult = Result.failure(e)
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            notesRepository.isSyncing.collect { isSyncing ->
                _uiState.update {
                    it.copy(isSyncing = isSyncing)
                }
            }
        }
    }

    fun resync() {
        _uiState.update {
            it.copy(isSyncing = true, syncResult = null)
        }
        notesRepository.doSync(isOnStart = false, fromScratch = true)
    }

    fun updateStatus() {
        viewModelScope.launch {
            notesRepository.readDatabaseStatus()
                .onSuccess { status ->
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

    }
}
