package org.dianqk.ruslin.ui.page.notes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.SubTitle
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
            ExpandableNavigationDrawerFolderItem(
                folder = folder,
                level = 0,
                selectedFolderId = if (conflictNoteFolderSelected) null else selectedFolderId,
                onClick = { clickedFolder ->
                    onSelectedFolderChanged(clickedFolder.ffiFolder)
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

@Composable
@ExperimentalMaterial3Api
fun ExpandableNavigationDrawerFolderItem(
    folder: Folder,
    level: Int,
    selectedFolderId: String?,
    onClick: (Folder) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = Shape32,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val expandable = folder.subFolders.isNotEmpty()
    val selected = folder.ffiFolder.id == selectedFolderId
    val isExpanded by folder.isExpanded.collectAsStateWithLifecycle()

    Surface(
        selected = selected,
        onClick = {
            onClick(folder)
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
                            text = folder.ffiFolder.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                    selectedFolderId = selectedFolderId,
                    onClick = onClick,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}