package org.dianqk.ruslin.ui.page.settings.tools.log

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun LogPage(
    viewModel: LogViewModel = hiltViewModel(),
    onPopBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.log),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) }
            )
        }
    ) {
        SelectionContainer(modifier = Modifier.padding(it)) {
            Text(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(horizontal = 5.dp),
                text = uiState.log
            )
        }
    }
}
