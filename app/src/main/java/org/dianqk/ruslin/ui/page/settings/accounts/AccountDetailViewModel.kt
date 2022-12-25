package org.dianqk.ruslin.ui.page.settings.accounts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dianqk.ruslin.data.*
import uniffi.ruslin.SyncConfig
import javax.inject.Inject

data class AccountDetailUiState(
    val email: String? = null,
    val url: String? = null,
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    @ApplicationContext private val context: Context,
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()
    val syncStrategy: StateFlow<SyncStrategy> = context.dataStore.syncStrategy().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SyncStrategy())

    init {
        loadSyncConfig()
    }

    fun setSyncInterval(syncInterval: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[DataStoreKeys.SyncInterval.key] = syncInterval
                }
            }
        }
    }

    fun setSyncOnStart(syncOnStart: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[DataStoreKeys.SyncOnStart.key] = syncOnStart
                }
            }
        }
    }

    fun setSyncOnlyWiFi(syncOnlyWiFi: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[DataStoreKeys.SyncOnlyWiFi.key] = syncOnlyWiFi
                }
            }
        }
    }

    fun setSyncOnlyWhenCharging(syncOnlyWhenCharging: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[DataStoreKeys.SyncOnlyWhenCharging.key] = syncOnlyWhenCharging
                }
            }
        }
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