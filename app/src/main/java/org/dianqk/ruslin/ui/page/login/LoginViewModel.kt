package org.dianqk.ruslin.ui.page.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dianqk.ruslin.data.NotesRepository
import uniffi.ruslin.SyncConfig
import javax.inject.Inject

data class LoginInfoUiState(
    val url: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginInfoUiState())
    val uiState: StateFlow<LoginInfoUiState> = _uiState.asStateFlow()

    fun setUrl(url: String) {
        _uiState.update {
            it.copy(
                url = url
            )
        }
    }

    fun setEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email
            )
        }
    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(
                password = password
            )
        }
    }

    suspend fun login(): Result<Unit> {
        val syncConfig = SyncConfig.JoplinServer(host = uiState.value.url, email = uiState.value.email, password = uiState.value.password)
        return notesRepository.saveSyncConfig(syncConfig)
    }

}