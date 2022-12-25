package org.dianqk.ruslin.ui.page.settings.accounts

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

data class AccountDetailUiState(
    val email: String? = null,
    val url: String? = null,
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    init {
        loadSyncConfig()
    }

    private fun loadSyncConfig() {
        viewModelScope.launch {
            notesRepository.getSyncConfig()
                .onSuccess { syncConfig ->
                    syncConfig?.let {
                        if (syncConfig is SyncConfig.JoplinServer) {
                            _uiState.update {
                                it.copy(
                                    email = syncConfig.email,
                                    url = syncConfig.host,
                                )
                            }
                        }
                    }
                }
                .onFailure { e ->

                }
        }
    }

}