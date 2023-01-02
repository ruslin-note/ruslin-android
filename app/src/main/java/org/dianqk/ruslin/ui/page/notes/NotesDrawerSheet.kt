package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.SubTitle
import uniffi.ruslin.FfiFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDrawerSheet(
    folders: List<FfiFolder>,
    selectedFolderId: String?,
    showConflictNoteFolder: Boolean,
    conflictNoteFolderSelected: Boolean,
    onSelectedConflictFolder: () -> Unit,
    onSelectedFolderChanged: (FfiFolder?) -> Unit,
    openCreateFolderDialog: Boolean,
    onCreateFolder: (String) -> Unit,
    onChangeOpenCreateFolderDialogVisible: (Boolean) -> Unit,
    onShowSettingsPage: () -> Unit
) {
    val createFolderTitle: MutableState<String> = remember { mutableStateOf("") }
    val scroll = rememberScrollState(0)

    if (openCreateFolderDialog) {
        CreateFolderDialog(
            onDismissRequest = {
                onChangeOpenCreateFolderDialogVisible(false)
                createFolderTitle.value = ""
            },
            onConfirmRequest = {
                onChangeOpenCreateFolderDialogVisible(false)
                onCreateFolder(createFolderTitle.value)
                createFolderTitle.value = ""
            },
            title = createFolderTitle.value,
            onTitleChanged = {
                createFolderTitle.value = it
            }
        )
    }
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scroll)
    ) {
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Article, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.all_notes)) },
            selected = !conflictNoteFolderSelected && selectedFolderId == null,
            onClick = {
                onSelectedFolderChanged(null)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(modifier = Modifier.height(12.dp))
        SubTitle(
            modifier = Modifier.padding(horizontal = 30.dp),
            text = stringResource(id = R.string.folders)
        )
        Spacer(modifier = Modifier.height(12.dp))
        folders.forEach { folder ->
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                label = { Text(folder.title) },
                selected = !conflictNoteFolderSelected && folder.id == selectedFolderId,
                onClick = {
                    onSelectedFolderChanged(folder)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.new_folder)) },
            selected = false,
            onClick = {
                onChangeOpenCreateFolderDialogVisible(true)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        if (showConflictNoteFolder) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Outlined.FolderCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.conflict_notes),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                selected = conflictNoteFolderSelected,
                onClick = onSelectedConflictFolder,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        Divider(modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            },
            label = { Text(text = stringResource(id = R.string.settings)) },
            selected = false,
            onClick = onShowSettingsPage,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: String,
    onTitleChanged: (String) -> Unit,
    focusRequest: FocusRequester = remember { FocusRequester() }
) {
    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) },
        title = {
            Text(text = stringResource(id = R.string.new_folder))
        },
        text = {
            TextField(
                modifier = Modifier.focusRequester(focusRequest),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ),
                value = title,
                onValueChange = onTitleChanged,
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotEmpty(),
                onClick = onConfirmRequest
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}
