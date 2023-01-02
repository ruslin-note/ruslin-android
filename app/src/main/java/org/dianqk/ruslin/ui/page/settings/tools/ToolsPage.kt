package org.dianqk.ruslin.ui.page.settings.tools

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsPage(
    navigateToLogDetail: () -> Unit,
    navigateToDatabaseStatus: () -> Unit,
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.tools),
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
                    text = stringResource(id = R.string.tools),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.log),
                    description = stringResource(id = R.string.view_logs),
                    icon = Icons.Filled.Description,
                    onClick = navigateToLogDetail
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.database_status),
                    description = stringResource(id = R.string.database_status_desc),
                    icon = ImageVector.vectorResource(id = R.drawable.ic_database),
                    onClick = navigateToDatabaseStatus
                )
            }
        }
    }
}
