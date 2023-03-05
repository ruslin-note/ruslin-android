package org.dianqk.ruslin.ui.page.note_detail

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.MarkdownRichText
import org.dianqk.ruslin.ui.component.MarkdownTextEditor
import org.dianqk.ruslin.ui.component.PrimaryTextTabs

private const val EDIT_TAB_INDEX = 0
private const val PREVIEW_TAB_INDEX = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPage(
    navigateToNote: (String) -> Unit,
    onPopBack: () -> Unit = {},
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val titles = listOf(stringResource(id = R.string.edit), stringResource(id = R.string.preview))
    var selectedTabIndex by remember { mutableStateOf(if (viewModel.isPreview) PREVIEW_TAB_INDEX else EDIT_TAB_INDEX) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    PrimaryTextTabs(
                        modifier = Modifier.width(160.dp),
                        titles = titles,
                        divider = {},
                        selectedTabIndex = selectedTabIndex,
                        onSelectedTabIndex = { index ->
                            selectedTabIndex = index
                            if (index == PREVIEW_TAB_INDEX) {
                                focusManager.clearFocus()
                                viewModel.updatePreviewHtml()
                            }
                        })
                },
                navigationIcon = {
                    BackButton(onClick = onPopBack)
                }
            )
        },
    ) { innerPadding ->
        MarkdownRichText(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .zIndex(if (selectedTabIndex == PREVIEW_TAB_INDEX) 1f else 0f)
                .background(MaterialTheme.colorScheme.background),
            htmlBodyText = uiState.previewHtml,
            navigateToNote = { noteId ->
                viewModel.saveNote()
                navigateToNote(noteId)
            })
        NoteEditor(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .zIndex(if (selectedTabIndex == EDIT_TAB_INDEX) 1f else 0f),
            title = uiState.title,
            body = uiState.body,
            onTitleChanged = viewModel::updateTitle,
            onBodyChanged = viewModel::updateBody,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditor(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onSaveResource: (Uri) -> SavedResource?,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
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
            textStyle = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Content),
            )
        Divider()
        MarkdownTextEditor(
            modifier = Modifier.weight(1f),
            onSaveResource = onSaveResource,
            value = body,
            onValueChange = onBodyChanged,
        )
    }
}
