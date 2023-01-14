package org.dianqk.ruslin.ui.page.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.NotesRepository
import uniffi.ruslin.SyncConfig
import javax.inject.Inject

data class LoginInfoUiState(
    val url: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    var loginSuccess: Boolean = false,
    val isLoggingIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginInfoUiState())
    val uiState: StateFlow<LoginInfoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notesRepository.getSyncConfig()
                .onSuccess { syncConfig ->
                    syncConfig?.let {
                        if (syncConfig is SyncConfig.JoplinServer) {
                            _uiState.update {
                                it.copy(
                                    email = syncConfig.email,
                                    url = syncConfig.host,
                                    password = syncConfig.password
                                )
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.toString())
                    }
                }
        }
    }

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

    fun dismissSnackBar() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun login() {
        _uiState.update {
            it.copy(isLoggingIn = true)
        }
        viewModelScope.launch {
            val syncConfig = SyncConfig.JoplinServer(
                host = uiState.value.url,
                email = uiState.value.email,
                password = uiState.value.password
            )
            notesRepository.saveSyncConfig(syncConfig)
                .onSuccess {
                    notesRepository.doSync(false)
                    _uiState.update {
                        it.copy(
                            isLoggingIn = false,
                            loginSuccess = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoggingIn = false,
                            errorMessage = e.toString()
                        )
                    }
                }
        }
    }
}
