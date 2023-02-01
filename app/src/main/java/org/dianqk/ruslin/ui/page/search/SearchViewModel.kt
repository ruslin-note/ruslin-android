package org.dianqk.ruslin.ui.page.search

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
import javax.inject.Inject

data class SearchUiState(
    val status: String = "",
    val searchTerm: String = "",
    val searchingTerm: String = "",
    val isSearching: Boolean = false,
    val searchedNotes: List<FfiSearchNote> = emptyList(),
    val notFound: Boolean = false,
    val showKeyboardOnFirstLoad: Boolean = true,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notesRepository.prepareJieba()
        }
    }

    fun changeSearchTerm(text: String) {
        _uiState.update {
            it.copy(searchTerm = text, notFound = false)
        }
    }

    fun hasShownKeyboardOnFirstLoad() {
        _uiState.update {
            it.copy(showKeyboardOnFirstLoad = false)
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
            notesRepository.search(searchTerm = searchTerm)
                .onSuccess { notes ->
                    _uiState.update {
                        it.copy(
                            searchedNotes = notes,
                            isSearching = false,
                            notFound = notes.isEmpty()
                        )
                    }
                }
        }
    }
}
