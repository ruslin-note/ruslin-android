package org.dianqk.ruslin.ui.page.settings.tools.database

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseStatusPage(
    viewModel: DatabaseStatusViewModel = hiltViewModel(),
    onPopBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncAngle by rememberInfiniteTransition().animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.database_status),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SelectionContainer {
                    Text(
                        text = uiState.status
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                uiState.syncResult?.let { result ->
                    result
                        .onSuccess { message ->
                            Text(text = message, color = MaterialTheme.colorScheme.primary)
                        }
                        .onFailure { e ->
                            Text(text = e.toString(), color = MaterialTheme.colorScheme.error)
                        }
                }
                Spacer(modifier = Modifier.height(5.dp))
                ElevatedButton(
                    enabled = !uiState.isSyncing,
                    onClick = viewModel::resync
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        modifier = Modifier.rotate(if (uiState.isSyncing) syncAngle else 360f),
                        contentDescription = null
                    )
                    Text(text = stringResource(id = R.string.resynchronize_from_scratch))
                }
            }
        }
    }
}
