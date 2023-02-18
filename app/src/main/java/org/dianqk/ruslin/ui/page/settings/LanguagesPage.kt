package org.dianqk.ruslin.ui.page.settings

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.LocalLanguages
import org.dianqk.ruslin.data.preference.LanguagesPreference
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesPage(
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val context = LocalContext.current
    val languages = LocalLanguages.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.languages),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item(key = languages.value + 10000) {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.languages),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            items(LanguagesPreference.values, key = { it.value }) {
                SettingItem(
                    title = it.getDesc(context),
                    onClick = { it.put(context = context, scope = scope) }
                ) {
                    RadioButton(selected = it == languages, onClick = {
                        it.put(context = context, scope = scope)
                    })
                }
            }
        }
    }
}