package org.dianqk.ruslin.ui.page.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.LocalContentTextDirection
import org.dianqk.ruslin.ui.component.CombinedClickableSurface
import org.dianqk.ruslin.ui.component.OutlinedButtonWithIcon
import org.dianqk.ruslin.ui.component.SubTitle
import org.dianqk.ruslin.ui.component.SuspendConfirmAlertDialog
import org.dianqk.ruslin.ui.theme.Shape32
import uniffi.ruslin.FfiFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDrawerSheet(
    folders: List<Folder>,
    selectedFolderId: String?,
    showConflictNoteFolder: Boolean,
    conflictNoteFolderSelected: Boolean,
    onSelectedConflictFolder: () -> Unit,
    onSelectedFolderChanged: (FfiFolder?) -> Unit,
    openCreateFolderDialog: Boolean,
    onCreateFolder: (String) -> Unit,
    onRenameFolder: (FfiFolder) -> Unit,
    onDeleteFolder: suspend (FfiFolder) -> Unit,
    onChangeOpenCreateFolderDialogVisible: (Boolean) -> Unit,
    onShowSettingsPage: () -> Unit
) {
    val scroll = rememberScrollState(0)
    var openEditFolderDialog: Folder? by remember {
        mutableStateOf(null)
    }
    var openDeleteFolderAlertDialog: Folder? by remember {
        mutableStateOf(null)
    }
    val contentTextDirection = LocalContentTextDirection.current

    openEditFolderDialog?.let { editFolder ->
        FolderDialog(
            isCreated = false,
            onDismissRequest = {
                openEditFolderDialog = null
            },
            onConfirmRequest = {
                onRenameFolder(editFolder.ffiFolder.copy(title = it))
                openEditFolderDialog = null
            },
            onDeleteRequest = {
                openEditFolderDialog = null
                openDeleteFolderAlertDialog = editFolder
            },
            initTitle = { editFolder.ffiFolder.title }
        )
    }

    openDeleteFolderAlertDialog?.let { deleteFolder ->
        SuspendConfirmAlertDialog(
            onDismissRequest = { openDeleteFolderAlertDialog = null },
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
                        id = R.string.ask_delete_folder_title,
                        deleteFolder.ffiFolder.title
                    )
                )
            },
            text = {
                Text(text = stringResource(id = R.string.ask_delete_folder_description))
            },
            onConfirm = {
                onDeleteFolder(deleteFolder.ffiFolder)
            }) {
            openDeleteFolderAlertDialog = null
        }
    }

    if (openCreateFolderDialog) {
        FolderDialog(
            onDismissRequest = {
                onChangeOpenCreateFolderDialogVisible(false)
            },
            onConfirmRequest = {
                onChangeOpenCreateFolderDialogVisible(false)
                onCreateFolder(it)
            },
            initTitle = { "" }
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
            ExpandableNavigationDrawerFolderItem(
                folder = folder,
                level = 0,
                titleTextDirection = contentTextDirection.getTextDirection(),
                selectedFolderId = if (conflictNoteFolderSelected) null else selectedFolderId,
                onClick = { clickedFolder ->
                    onSelectedFolderChanged(clickedFolder.ffiFolder)
                },
                openEditFolderDialog = { editFolder ->
                    openEditFolderDialog = editFolder
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

@Composable
@ExperimentalMaterial3Api
fun ExpandableNavigationDrawerFolderItem(
    folder: Folder,
    level: Int,
    titleTextDirection: TextDirection,
    selectedFolderId: String?,
    onClick: (Folder) -> Unit,
    openEditFolderDialog: (Folder) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = Shape32,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val expandable = folder.subFolders.isNotEmpty()
    val selected = folder.ffiFolder.id == selectedFolderId
    val isExpanded by folder.isExpanded.collectAsStateWithLifecycle()

    CombinedClickableSurface(
        onClick = {
            onClick(folder)
        },
        onLongClick = {
            openEditFolderDialog(folder)
        },
        modifier = modifier
            .height(56.dp)
            .padding(start = (level * 20).dp)
            .fillMaxWidth(),
        shape = shape,
        color = colors.containerColor(selected).value,
        interactionSource = interactionSource,
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = if (expandable) 0.dp else 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconColor = colors.iconColor(selected).value
            CompositionLocalProvider(
                LocalContentColor provides iconColor,
                content = { Icon(Icons.Outlined.Folder, contentDescription = null) })
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f)) {
                val labelColor = colors.textColor(selected).value
                CompositionLocalProvider(
                    LocalContentColor provides labelColor,
                    content = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = folder.ffiFolder.title,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyLarge.copy(textDirection = titleTextDirection),
                            overflow = TextOverflow.Ellipsis,
                        )
                    })
            }
            if (expandable) {
                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.1f))
                        .clickable {
                            folder.setExpanded(!isExpanded)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            for (subFolder in folder.subFolders) {
                ExpandableNavigationDrawerFolderItem(
                    folder = subFolder,
                    level = level + 1,
                    titleTextDirection = titleTextDirection,
                    selectedFolderId = selectedFolderId,
                    onClick = onClick,
                    openEditFolderDialog = openEditFolderDialog,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: (String) -> Unit,
    onDeleteRequest: () -> Unit = {},
    isCreated: Boolean = true,
    initTitle: () -> String,
    focusRequest: FocusRequester = remember { FocusRequester() }
) {
    var folderTitle by remember { mutableStateOf(initTitle()) }

    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                if (isCreated) Icons.Filled.CreateNewFolder else Icons.Filled.Edit,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = if (isCreated) R.string.new_folder else R.string.edit_folder))
        },
        text = {
            TextField(
                modifier = Modifier.focusRequester(focusRequest),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                value = folderTitle,
                onValueChange = {
                    folderTitle = it
                },
                singleLine = true
            )
        },
        confirmButton = {
            Row(modifier = Modifier) {
                TextButton(
                    enabled = folderTitle.isNotEmpty(),
                    onClick = {
                        onConfirmRequest(folderTitle)
                    }
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }
        },
        dismissButton = {
            if (!isCreated) {
                OutlinedButtonWithIcon(
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    onClick = onDeleteRequest,
                    icon = Icons.Outlined.Delete,
                    text = stringResource(id = R.string.delete)
                )
            }
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}
