package org.dianqk.ruslin.ui.page.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import uniffi.ruslin.FfiSearchNote

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLifecycleComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SearchPage(
    viewModel: SearchViewModel = hiltViewModel(),
    navigateToNoteDetail: (noteId: String) -> Unit,
    onPopBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequest = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.search),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequest),
                singleLine = true,
                value = uiState.searchTerm,
                onValueChange = viewModel::changeSearchTerm,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.search()
                    }
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (uiState.notFound) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.not_found, uiState.searchTerm)
                )
            }
            if (uiState.isSearching) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        text = stringResource(id = R.string.searching, uiState.searchingTerm)
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                items(
                    items = uiState.searchNotes,
                    key = { it.id }
                ) { note ->
                    SearchNote(
                        note = note,
                        navigateToNoteDetail = navigateToNoteDetail
                    )
                }
            }
        }
    }

    if (lazyListState.isScrollInProgress) {
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }
}

@Composable
fun SearchNote(note: FfiSearchNote, navigateToNoteDetail: (noteId: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navigateToNoteDetail(note.id)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}
