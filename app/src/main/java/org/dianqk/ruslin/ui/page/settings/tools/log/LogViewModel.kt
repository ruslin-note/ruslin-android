package org.dianqk.ruslin.ui.page.settings.tools.log

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

data class LogUiState(
    val log: String = ""
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val log = notesRepository.readLog()
            _uiState.update {
                it.copy(
                    log = log
                )
            }
        }
    }

}