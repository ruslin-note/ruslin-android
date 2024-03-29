package org.dianqk.ruslin.ui.page.settings.accounts

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.NotesRepository
import org.dianqk.ruslin.data.SyncStrategy
import org.dianqk.ruslin.data.dataStore
import org.dianqk.ruslin.data.syncStrategy
import uniffi.ruslin.SyncConfig
import javax.inject.Inject

data class AccountDetailUiState(
    val email: String? = null,
    val url: String? = null
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()
    private val dataStore = context.dataStore
    val syncStrategy: StateFlow<SyncStrategy> = dataStore.syncStrategy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SyncStrategy())

    init {
        loadSyncConfig()
    }

    fun setSyncInterval(syncInterval: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.edit {
                    it[DataStoreKeys.SyncInterval.key] = syncInterval
                }
            }
        }
    }

    fun setSyncOnStart(syncOnStart: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.edit {
                    it[DataStoreKeys.SyncOnStart.key] = syncOnStart
                }
            }
        }
    }

    fun setSyncOnlyWiFi(syncOnlyWiFi: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.edit {
                    it[DataStoreKeys.SyncOnlyWiFi.key] = syncOnlyWiFi
                }
            }
        }
    }

    fun setSyncOnlyWhenCharging(syncOnlyWhenCharging: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataStore.edit {
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
                                    url = syncConfig.host
                                )
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            email = e.toString(),
                            url = e.toString(),
                        )
                    }
                }
        }
    }
}
