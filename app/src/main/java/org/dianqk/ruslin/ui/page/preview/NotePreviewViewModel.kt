package org.dianqk.ruslin.ui.page.preview

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import org.dianqk.ruslin.ui.RuslinDestinationsArgs
import org.dianqk.ruslin.ui.page.note_detail.TAG
import uniffi.ruslin.parseMarkdownToHtml
import javax.inject.Inject

data class NotePreviewUiState(
    val isLoading: Boolean = true,
    val htmlText: String? = null,
)

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val noteId: String? = savedStateHandle[RuslinDestinationsArgs.NOTE_ID_ARG]

    private val _uiState = MutableStateFlow(NotePreviewUiState())
    val uiState: StateFlow<NotePreviewUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            notesRepository.loadNote(noteId)
                .onSuccess { note ->
                    withContext(Dispatchers.Default) {
                        val htmlText = buildString {
                            append(
                                """
                                <html>
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1">
                                        <link rel="stylesheet" href="ruslin-assets:///github-markdown.min.css">
                                        <style>
                                            .markdown-body {
                                                box-sizing: border-box;
                                                min-width: 200px;
                                                max-width: 980px;
                                                margin: 0 auto;
                                                padding: 45px;
                                            }
    
                                            @media (max-width: 767px) {
                                                .markdown-body {
                                                    padding: 15px;
                                                }
                                            }
                                            p > img {
                                              display: block;
                                              margin-left: auto;
                                              margin-right: auto;
                                              max-height: 260px;
                                              max-width: 100%;
                                            }
                                        </style>
                                    </head>
                                    <article class="markdown-body">
                            """.trimIndent()
                            )
                            append(parseMarkdownToHtml(note.body))
                            append("</article></html>")
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                htmlText = htmlText,
                            )
                        }
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
