package org.dianqk.ruslin.ui.page.settings.accounts

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.SyncIntervalPreference
import org.dianqk.ruslin.ui.component.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AccountDetailPage(
    onPopBack: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStrategy by viewModel.syncStrategy.collectAsStateWithLifecycle()
    var syncIntervalDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.account),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.account),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                PreferenceSubtitle(text = stringResource(id = R.string.joplin_server))
                SettingItem(
                    title = uiState.email ?: stringResource(id = R.string.account),
                    description = uiState.url ?: stringResource(id = R.string.account_setting_desc),
                    icon = Icons.Filled.ManageAccounts,
                    onClick = navigateToLogin,
                )
            }
            item {
                PreferenceSubtitle(text = stringResource(id = R.string.syncing))
                SettingItem(
                    title = stringResource(id = R.string.sync_interval),
                    description = SyncIntervalPreference.toSyncInterval(syncStrategy.syncInterval).toDesc(context),
                    icon = Icons.Filled.EventRepeat,
                    onClick = { syncIntervalDialogVisible = true },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.sync_once_on_start),
                    isChecked = syncStrategy.syncOnStart,
                    onClick = {
                        viewModel.setSyncOnStart(!syncStrategy.syncOnStart)
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.only_on_wifi),
                    isChecked = syncStrategy.syncOnlyWiFi,
                    onClick = {
                        viewModel.setSyncOnlyWiFi(!syncStrategy.syncOnlyWiFi)
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.only_when_charging),
                    isChecked = syncStrategy.syncOnlyWhenCharging,
                    onClick = {
                        viewModel.setSyncOnlyWhenCharging(!syncStrategy.syncOnlyWhenCharging)
                    },
                )
            }
        }
    }

    if (syncIntervalDialogVisible) {
        RadioDialog(
            title = stringResource(R.string.sync_interval),
            options = SyncIntervalPreference.values.map {
                RadioDialogOption(
                    text = it.toDesc(context),
                    selected = it.value == syncStrategy.syncInterval,
                ) {
                    viewModel.setSyncInterval(it.value)
                }
            }
        ) {
            syncIntervalDialogVisible = false
        }

    }

}
