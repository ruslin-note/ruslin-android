package org.dianqk.ruslin.ui.page.settings

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navigateToAccountDetail: () -> Unit,
    navigateToTools: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToAppearance: () -> Unit,
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
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
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.account),
                    description = stringResource(id = R.string.account_setting_desc),
                    icon = Icons.Filled.AccountCircle,
                    onClick = navigateToAccountDetail
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.color_and_style),
                    description = stringResource(id = R.string.color_and_style_setting_desc),
                    icon = Icons.Filled.Palette,
                    onClick = navigateToAppearance
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.languages),
                    description = stringResource(id = R.string.languages_setting_desc),
                    icon = Icons.Filled.Language,
                    onClick = navigateToLanguages,
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.tools),
                    description = stringResource(id = R.string.tools_setting_des),
                    icon = Icons.Filled.Build,
                    onClick = navigateToTools
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.about),
                    description = stringResource(id = R.string.about_setting_desc),
                    icon = Icons.Filled.TipsAndUpdates,
                    onClick = { navigateToAbout() }
                )
            }
        }
    }
}
