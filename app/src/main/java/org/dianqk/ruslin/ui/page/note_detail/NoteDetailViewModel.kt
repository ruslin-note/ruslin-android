package org.dianqk.ruslin.ui.page.note_detail

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject

data class NoteDetailUiState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false
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
}
