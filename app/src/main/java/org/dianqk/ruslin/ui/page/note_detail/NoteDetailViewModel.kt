package org.dianqk.ruslin.ui.page.note_detail

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import uniffi.ruslin.FfiNote
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

data class NoteDetailUiState(
    val noteId: String? = null,
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val previewHtml: String = "<!DOCTYPE html><html><body></body><html>"
)

const val TAG = "NoteDetailViewModel"

data class SavedResource(
    val id: String,
    val isImage: Boolean,
    val filename: String,
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: String? = savedStateHandle[RuslinDestinationsArgs.FOLDER_ID_ARG]
    private val noteId: String? = savedStateHandle[RuslinDestinationsArgs.NOTE_ID_ARG]
    val isPreview: Boolean = savedStateHandle[RuslinDestinationsArgs.IS_PREVIEW_ARG] ?: false
    private var note: FfiNote? = null
    private var edited: Boolean = false

    private val _uiState = MutableStateFlow(NoteDetailUiState(noteId = noteId))
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
        val title: String = uiState.value.title.ifEmpty {
            uiState.value.body.split("\n").getOrNull(0) ?: ""
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
            edited = false
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
                    if (isPreview) {
                        setPreviewHtml(body = note.body)
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

    fun saveResource(context: Context, uri: Uri): SavedResource? {
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val isImage = mime.startsWith("image/")
        return context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val filename = cursor.getString(nameIndex)
            val size = cursor.getInt(sizeIndex)
            val fileExtension = filename.substring(filename.lastIndexOf('.') + 1)

            val resource = notesRepository.createResource(
                title = filename,
                mime = mime,
                fileExtension = fileExtension,
                size = size
            )
            val resourceDir = notesRepository.resourceDir
            val resourceFile = resourceDir.resolve("${resource.id}.${resource.fileExtension}")
            resourceFile.createNewFile()
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val outputStream: OutputStream = FileOutputStream(resourceFile)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
            cursor.close()
            viewModelScope.launch {
                notesRepository.saveResource(resource)
                    .onFailure { e ->
                        throw e
                    }
            }
            return@let SavedResource(id = resource.id, isImage = isImage, filename = filename)
        }
    }

    fun setPreviewHtml(body: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // TODO: match m3 theme
                val previewHtml = buildString {
                    append(
                        """
                                <!DOCTYPE html>
                                <html>
                                    <head>
                                        <meta charset="UTF-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                                        <link rel="stylesheet" href="ruslin-assets:///github-markdown.min.css">
                                        <style>
                                            @media (prefers-color-scheme:dark) {
                                                .markdown-body {
                                                    color-scheme: dark;
                                                    --color-prettylights-syntax-comment: #8b949e;
                                                    --color-prettylights-syntax-constant: #79c0ff;
                                                    --color-prettylights-syntax-entity: #d2a8ff;
                                                    --color-prettylights-syntax-storage-modifier-import: #c9d1d9;
                                                    --color-prettylights-syntax-entity-tag: #7ee787;
                                                    --color-prettylights-syntax-keyword: #ff7b72;
                                                    --color-prettylights-syntax-string: #a5d6ff;
                                                    --color-prettylights-syntax-variable: #ffa657;
                                                    --color-prettylights-syntax-brackethighlighter-unmatched: #f85149;
                                                    --color-prettylights-syntax-invalid-illegal-text: #f0f6fc;
                                                    --color-prettylights-syntax-invalid-illegal-bg: #8e1519;
                                                    --color-prettylights-syntax-carriage-return-text: #f0f6fc;
                                                    --color-prettylights-syntax-carriage-return-bg: #b62324;
                                                    --color-prettylights-syntax-string-regexp: #7ee787;
                                                    --color-prettylights-syntax-markup-list: #f2cc60;
                                                    --color-prettylights-syntax-markup-heading: #1f6feb;
                                                    --color-prettylights-syntax-markup-italic: #c9d1d9;
                                                    --color-prettylights-syntax-markup-bold: #c9d1d9;
                                                    --color-prettylights-syntax-markup-deleted-text: #ffdcd7;
                                                    --color-prettylights-syntax-markup-deleted-bg: #67060c;
                                                    --color-prettylights-syntax-markup-inserted-text: #aff5b4;
                                                    --color-prettylights-syntax-markup-inserted-bg: #033a16;
                                                    --color-prettylights-syntax-markup-changed-text: #ffdfb6;
                                                    --color-prettylights-syntax-markup-changed-bg: #5a1e02;
                                                    --color-prettylights-syntax-markup-ignored-text: #c9d1d9;
                                                    --color-prettylights-syntax-markup-ignored-bg: #1158c7;
                                                    --color-prettylights-syntax-meta-diff-range: #d2a8ff;
                                                    --color-prettylights-syntax-brackethighlighter-angle: #8b949e;
                                                    --color-prettylights-syntax-sublimelinter-gutter-mark: #484f58;
                                                    --color-prettylights-syntax-constant-other-reference-link: #a5d6ff;
                                                    --color-fg-default: #c9d1d9;
                                                    --color-fg-muted: #8b949e;
                                                    --color-fg-subtle: #484f58;
                                                    --color-canvas-default: #0d1117;
                                                    --color-canvas-subtle: #161b22;
                                                    --color-border-default: #30363d;
                                                    --color-border-muted: #21262d;
                                                    --color-neutral-muted: rgba(110, 118, 129, 0.4);
                                                    --color-accent-fg: #58a6ff;
                                                    --color-accent-emphasis: #1f6feb;
                                                    --color-attention-subtle: rgba(187, 128, 9, 0.15);
                                                    --color-danger-fg: #f85149
                                                }
                                            }

                                            @media (prefers-color-scheme:light) {
                                                .markdown-body {
                                                    color-scheme: light;
                                                    --color-prettylights-syntax-comment: #6e7781;
                                                    --color-prettylights-syntax-constant: #0550ae;
                                                    --color-prettylights-syntax-entity: #8250df;
                                                    --color-prettylights-syntax-storage-modifier-import: #24292f;
                                                    --color-prettylights-syntax-entity-tag: #116329;
                                                    --color-prettylights-syntax-keyword: #cf222e;
                                                    --color-prettylights-syntax-string: #0a3069;
                                                    --color-prettylights-syntax-variable: #953800;
                                                    --color-prettylights-syntax-brackethighlighter-unmatched: #82071e;
                                                    --color-prettylights-syntax-invalid-illegal-text: #f6f8fa;
                                                    --color-prettylights-syntax-invalid-illegal-bg: #82071e;
                                                    --color-prettylights-syntax-carriage-return-text: #f6f8fa;
                                                    --color-prettylights-syntax-carriage-return-bg: #cf222e;
                                                    --color-prettylights-syntax-string-regexp: #116329;
                                                    --color-prettylights-syntax-markup-list: #3b2300;
                                                    --color-prettylights-syntax-markup-heading: #0550ae;
                                                    --color-prettylights-syntax-markup-italic: #24292f;
                                                    --color-prettylights-syntax-markup-bold: #24292f;
                                                    --color-prettylights-syntax-markup-deleted-text: #82071e;
                                                    --color-prettylights-syntax-markup-deleted-bg: #FFEBE9;
                                                    --color-prettylights-syntax-markup-inserted-text: #116329;
                                                    --color-prettylights-syntax-markup-inserted-bg: #dafbe1;
                                                    --color-prettylights-syntax-markup-changed-text: #953800;
                                                    --color-prettylights-syntax-markup-changed-bg: #ffd8b5;
                                                    --color-prettylights-syntax-markup-ignored-text: #eaeef2;
                                                    --color-prettylights-syntax-markup-ignored-bg: #0550ae;
                                                    --color-prettylights-syntax-meta-diff-range: #8250df;
                                                    --color-prettylights-syntax-brackethighlighter-angle: #57606a;
                                                    --color-prettylights-syntax-sublimelinter-gutter-mark: #8c959f;
                                                    --color-prettylights-syntax-constant-other-reference-link: #0a3069;
                                                    --color-fg-default: #24292f;
                                                    --color-fg-muted: #57606a;
                                                    --color-fg-subtle: #6e7781;
                                                    --color-canvas-default: #ffffff;
                                                    --color-canvas-subtle: #f6f8fa;
                                                    --color-border-default: #d0d7de;
                                                    --color-border-muted: hsla(210, 18%, 87%, 1);
                                                    --color-neutral-muted: rgba(175, 184, 193, 0.2);
                                                    --color-accent-fg: #0969da;
                                                    --color-accent-emphasis: #0969da;
                                                    --color-attention-subtle: #fff8c5;
                                                    --color-danger-fg: #cf222e
                                                }
                                            }
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
                    append(notesRepository.parseMarkdownToPreviewHtml(body))
                    append("</article></html>")
                }
                _uiState.update {
                    it.copy(previewHtml = previewHtml)
                }
            }
        }
    }

    fun updatePreviewHtml() {
        if (!edited) {
            return
        }
        setPreviewHtml(body = _uiState.value.body)
    }
}
