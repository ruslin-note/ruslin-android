package org.dianqk.ruslin.ui.page.notes

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.ContentEmptyState
import org.dianqk.ruslin.ui.component.ContentLoadingState
import org.dianqk.ruslin.ui.component.SuspendConfirmAlertDialog

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun NotesPage(
    viewModel: NotesViewModel = hiltViewModel(),
    navigateToNote: (String) -> Unit,
    navigateToNewNote: (String?) -> Unit,
    navigateToLogin: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToSearch: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notes = uiState.items

    val openCreateFolderDialog = remember { mutableStateOf(false) }

    val syncAngle by rememberInfiniteTransition().animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    val topAppBarTitle = if (uiState.showConflictNotes) {
        stringResource(id = R.string.conflict_notes)
    } else {
        uiState.selectedFolder?.title
            ?: stringResource(id = R.string.all_notes)
    }

    var showRemoveMultipleItemsDialog by remember { mutableStateOf(false) }
    var firstSelectedItemId: String? by remember { mutableStateOf(null) }
    val isSelectEnabled = firstSelectedItemId != null
    val selectedItemIds = remember(uiState.items, firstSelectedItemId) {
        firstSelectedItemId?.let { mutableStateListOf(it) } ?: mutableStateListOf()
    }

    BackHandler(isSelectEnabled) {
        firstSelectedItemId = null
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isSelectEnabled,
        drawerContent = {
            NotesDrawerSheet(
                folders = uiState.folders,
                selectedFolderId = uiState.selectedFolder?.id,
                showConflictNoteFolder = uiState.conflictNoteExists,
                conflictNoteFolderSelected = uiState.showConflictNotes,
                onSelectedConflictFolder = {
                    scope.launch { drawerState.close() }
                    viewModel.showConflictNotes()
                },
                onSelectedFolderChanged = {
                    scope.launch { drawerState.close() }
                    viewModel.selectFolder(it)
                },
                onCreateFolder = viewModel::createFolder,
                onRenameFolder = viewModel::changeFolder,
                onDeleteFolder = viewModel::deleteFolder,
                openCreateFolderDialog = openCreateFolderDialog.value,
                onChangeOpenCreateFolderDialogVisible = {
                    openCreateFolderDialog.value = it
                },
                onShowSettingsPage = {
                    navigateToSettings()
                }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = topAppBarTitle
                            )
                        },
                        navigationIcon = {
                            if (isSelectEnabled) {
                                IconButton(onClick = { firstSelectedItemId = null }) {
                                    Icon(Icons.Default.Close, stringResource(id = R.string.cancel))
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        stringResource(id = R.string.desc_menu)
                                    )
                                }
                            }
                        },
                        actions = {
                            if (isSelectEnabled) {
                                IconButton(
                                    onClick = {
                                        showRemoveMultipleItemsDialog = true
                                    },
                                    enabled = selectedItemIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteForever,
                                        contentDescription = stringResource(id = R.string.delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                IconButton(onClick = { navigateToSearch() }) {
                                    Icon(
                                        Icons.Default.Search,
                                        stringResource(id = R.string.desc_search)
                                    )
                                }
                                IconButton(
                                    enabled = !uiState.isSyncing,
                                    onClick = {
                                        if (viewModel.syncConfigExists()) {
                                            viewModel.sync()
                                        } else {
                                            navigateToLogin()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = stringResource(id = R.string.desc_sync),
                                        modifier = Modifier.rotate(
                                            if (uiState.isSyncing) syncAngle else 360f
                                        )
                                    )
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (!uiState.showConflictNotes && !isSelectEnabled) {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(id = R.string.new_note)) },
                            icon = {
                                Icon(
                                    Icons.Default.Edit,
                                    stringResource(id = R.string.new_note)
                                )
                            },
                            onClick = { navigateToNewNote(uiState.selectedFolder?.id) }
                        )
                    }
                }
            ) { innerPadding ->
                uiState.syncResult?.onFailure { e ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.onError)
                            .zIndex(1f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.sync_failed),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                SelectionContainer {
                                    Text(
                                        text = e.localizedMessage ?: e.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            IconButton(onClick = viewModel::dismissSyncErrorMessage) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                if (notes == null) {
                    ContentLoadingState {
                        Text(text = stringResource(id = R.string.notes_loading))
                    }
                } else if (notes.isEmpty()) {
                    ContentEmptyState {
                        Text(text = stringResource(id = R.string.create_a_note))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteAbbrCard(
                                note = note,
                                isSelectEnabled = { isSelectEnabled },
                                isSelected = { selectedItemIds.contains(note.id) },
                                onSelect = {
                                    if (selectedItemIds.contains(note.id)) selectedItemIds.remove(
                                        note.id
                                    )
                                    else selectedItemIds.add(note.id)
                                },
                                onClick = { navigateToNote(note.id) },
                                onLongClick = {
                                    firstSelectedItemId = note.id
                                })
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    )

    if (showRemoveMultipleItemsDialog) {
        SuspendConfirmAlertDialog(
            onDismissRequest = { showRemoveMultipleItemsDialog = false },
            inProgressIcon = {
                Icon(
                    modifier = it,
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(
                        id = R.string.ask_delete_selected_notes
                    )
                )
            },
            onConfirm = {
                viewModel.deleteNotes(selectedItemIds)
                    .onFailure { e ->
                        Log.d(TAG, "$e")
                    }
            }) {
            firstSelectedItemId = null
            showRemoveMultipleItemsDialog = false
        }
    }

    LaunchedEffect(Unit) {
        drawerState.snapTo(DrawerValue.Closed)
    }
}
