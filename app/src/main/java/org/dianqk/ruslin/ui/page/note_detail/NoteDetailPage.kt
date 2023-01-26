package org.dianqk.ruslin.ui.page.note_detail

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.MarkdownTextEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPage(
    navigationPreview: (String) -> Unit,
    onPopBack: () -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(onClick = onPopBack)
                },
                actions = {
                    uiState.noteId?.let { noteId ->
                        IconButton(onClick = { navigationPreview(noteId) }) {
                            Icon(Icons.Default.Preview, stringResource(id = R.string.desc_more))
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        NoteDetailContent(
            loading = uiState.isLoading,
            title = uiState.title,
            body = uiState.body,
            onTitleChanged = viewModel::updateTitle,
            onBodyChanged = viewModel::updateBody,
            modifier = Modifier
                .padding(innerPadding),
            onSaveResource = { uri: Uri ->
                viewModel.saveResource(context = context, uri = uri)
            },
        )
    }

    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveNote()
            }
        }
        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)
        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalLayoutApi::class,
)
@Composable
private fun NoteDetailContent(
    modifier: Modifier = Modifier,
    loading: Boolean,
    title: String,
    body: String,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onSaveResource: (Uri) -> SavedResource?,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    if (loading) {
        val pullRefreshState =
            rememberPullRefreshState(refreshing = true, onRefresh = { /* DO NOTHING */ })
        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            PullRefreshIndicator(
                refreshing = true,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .bringIntoViewRequester(bringIntoViewRequester)
                .imePadding()
        ) {
            TextField(
                value = title,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = Color.Transparent
                ),
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge
//                singleLine = true,
            )
            Divider(modifier = Modifier.fillMaxWidth())
            MarkdownTextEditor(
                modifier = Modifier.weight(1f),
                onSaveResource = onSaveResource,
                value = body,
                onValueChange = {
                    onBodyChanged(it)
                }
            )
        }
    }
}
