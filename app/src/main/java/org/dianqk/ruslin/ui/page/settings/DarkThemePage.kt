package org.dianqk.ruslin.ui.page.settings

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.LocalDarkTheme
import org.dianqk.ruslin.data.LocalHighContrastDarkTheme
import org.dianqk.ruslin.data.LocalLanguages
import org.dianqk.ruslin.data.preference.DarkThemePreference
import org.dianqk.ruslin.data.preference.LanguagesPreference
import org.dianqk.ruslin.data.preference.not
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.PreferenceSubtitle
import org.dianqk.ruslin.ui.component.PreferenceSwitch
import org.dianqk.ruslin.ui.component.SettingItem
import org.dianqk.ruslin.ui.component.SubTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkThemePage(
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val context = LocalContext.current
    val darkTheme = LocalDarkTheme.current
    val scope = rememberCoroutineScope()
    val highContrastDarkTheme = LocalHighContrastDarkTheme.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.dark_theme),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.dark_theme),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            items(DarkThemePreference.values, key = { it.value }) {
                SettingItem(
                    title = it.toDesc(context),
                    onClick = { it.put(context = context, scope = scope) }
                ) {
                    RadioButton(selected = it == darkTheme, onClick = {
                        it.put(context = context, scope = scope)
                    })
                }
            }
            item {
                PreferenceSubtitle(text = stringResource(id = R.string.other))
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.high_contrast),
                    icon = Icons.Outlined.Contrast,
                    isChecked = highContrastDarkTheme.value,
                    onClick = {
                        (!highContrastDarkTheme).put(context, scope)
                    }
                )
            }
        }
    }
}