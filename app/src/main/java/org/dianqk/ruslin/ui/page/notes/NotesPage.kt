package org.dianqk.ruslin.ui.page.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun NotesPage(
    navigateToNoteDetail: (parentId: String?, noteId: String?) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val openCreateFolderDialog = remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NotesDrawerSheet(
                folders = uiState.folders,
                selectedFolder = uiState.selectedFolder,
                onSelectedFolderChanged = {
                    scope.launch { drawerState.close() }
                    viewModel.selectFolder(it)
                },
                onCreateFolder = viewModel::createFolder,
                openCreateFolderDialog = openCreateFolderDialog.value,
                onChangeOpenCreateFolderDialogVisible = {
                    openCreateFolderDialog.value = it
                },
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = uiState.selectedFolder?.title
                                    ?: stringResource(id = R.string.all_notes),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, stringResource(id = R.string.desc_menu))
                            }
                        },
                        actions = {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    Icons.Default.Search,
                                    stringResource(id = R.string.desc_search)
                                )
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    stringResource(id = R.string.desc_more)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(id = R.string.new_note)) },
                        icon = { Icon(Icons.Default.Edit, stringResource(id = R.string.new_note)) },
                        onClick = { navigateToNoteDetail(uiState.selectedFolder?.id, null) })
                }
            ) { innerPadding ->
                NoteList(
                    notes = uiState.items,
                    navigateToNoteDetail = {
                        navigateToNoteDetail(uiState.selectedFolder?.id, it)
                    },
                    paddingValues = innerPadding
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.loadAbbrNotes()
    }
}
