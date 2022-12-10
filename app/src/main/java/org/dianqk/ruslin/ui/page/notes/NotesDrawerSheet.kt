package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R

data class Folder(
    val id: String,
    val name: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDrawerSheet(
    folders: List<Folder>,
    selectedFolder: Folder?,
    onSelectedFolderChanged: (Folder?) -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Article, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.all_notes)) },
            selected = selectedFolder == null,
            onClick = {
                      onSelectedFolderChanged(null)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(12.dp))
        folders.forEach { folder ->
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                label = { Text(folder.name) },
                selected = folder.id == selectedFolder?.id,
                onClick = {
                          onSelectedFolderChanged(folder)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}