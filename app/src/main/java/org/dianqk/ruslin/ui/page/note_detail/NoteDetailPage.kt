package org.dianqk.ruslin.ui.page.note_detail

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.EditorToolBar
import org.dianqk.ruslin.ui.component.MarkdownInsertTagType
import org.dianqk.ruslin.ui.component.MarkdownTextEditor
import uniffi.ruslin.FfiResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPage(
    onPopBack: () -> Unit = {},
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(onClick = onPopBack)
                }
//                actions = {
//                    IconButton(onClick = { /*TODO*/ }) {
//                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.desc_more))
//                    }
//                }
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
            createFfiResource = viewModel::createFfiResource,
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
    createFfiResource: () -> FfiResource,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val markdownInsertTag = remember {
        MutableSharedFlow<MarkdownInsertTagType>(replay = 0)
    }

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
                createFfiResource = createFfiResource,
                value = body,
                onValueChange = {
                    onBodyChanged(it)
                }
            )
        }
    }
}
