package org.dianqk.ruslin.ui.page.search

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
import uniffi.ruslin.FfiSearchNote
import uniffi.ruslin.FfiStatus
import javax.inject.Inject

data class SearchUiState(
    val status: String = "",
    val searchTerm: String = "",
    val searchingTerm: String = "",
    val isSearching: Boolean = false,
    val searchNotes: List<FfiSearchNote> = emptyList(),
    val notFound: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun changeSearchTerm(text: String) {
        _uiState.update {
            it.copy(searchTerm = text, notFound = false)
        }
    }

    fun search() {
        if (_uiState.value.isSearching) {
            return
        }
        val searchTerm = _uiState.value.searchTerm
        _uiState.update {
            it.copy(isSearching = true, notFound = false, searchingTerm = searchTerm)
        }
        viewModelScope.launch {
            notesRepository.search(searchTerm = searchTerm, enableHighlight = false)
                .onSuccess { notes ->
                    _uiState.update {
                        it.copy(searchNotes = notes, isSearching = false, notFound = notes.isEmpty())
                    }
                }
        }
    }

}