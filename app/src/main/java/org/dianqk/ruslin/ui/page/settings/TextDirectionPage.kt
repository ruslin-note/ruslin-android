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
import org.dianqk.ruslin.data.LocalContentTextDirection
import org.dianqk.ruslin.data.preference.TextDirectionPreference
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDirectionPage(
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val textDirection = LocalContentTextDirection.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.content_text_direction),
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
                    text = stringResource(id = R.string.content_text_direction),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            items(TextDirectionPreference.values, key = { it.value }) {
                SettingItem(
                    title = it.toDesc(context),
                    onClick = { it.put(context = context, scope = scope) }
                ) {
                    RadioButton(selected = it == textDirection, onClick = {
                        it.put(context = context, scope = scope)
                    })
                }
            }
        }
    }
}