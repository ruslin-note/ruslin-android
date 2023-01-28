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
                                            @media (prefers-color-scheme: dark) {
                                                html {
                                                    background-color: #0d1117;
                                                }
                                            }
                                            @media (prefers-color-scheme: light) {
                                                html {
                                                    background-color: #ffffff;
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
