package org.dianqk.ruslin.ui.page.settings.accounts

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.PreferenceSubtitle
import org.dianqk.ruslin.ui.component.PreferenceSwitch
import org.dianqk.ruslin.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AccountDetailPage(
    onPopBack: () -> Unit,
    navigateToLogin: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val enabled = remember { mutableStateOf(false) }

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
                    description = stringResource(id = R.string.every_30_minutes),
                    icon = Icons.Filled.EventRepeat,
                    onClick = { /* TODO */ },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.sync_once_on_start),
                    isChecked = enabled.value,
                    onClick = {
                        enabled.value = !enabled.value
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.only_on_wifi),
                    isChecked = enabled.value,
                    onClick = {
                        enabled.value = !enabled.value
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.only_when_charging),
                    isChecked = enabled.value,
                    onClick = {
                        enabled.value = !enabled.value
                    },
                )
            }
        }
    }
}
