package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(
    navigateToNoteDetail: (String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val notes = listOf<NoteAbbr>(
        NoteAbbr("1", "Compose for Wear OS 1.1 is now stable: check out new features!"),
        NoteAbbr("2", "Introducing the Architecture Templates"),
        NoteAbbr("3", "Android 13 for TV is now available"),
        NoteAbbr("4", "Blurring the Lines"),
        NoteAbbr("5", "Jetpack Compose — When should I use derivedStateOf?"),
        NoteAbbr("6", "Google Play Coffee break with Creatrip"),
        NoteAbbr("7", "RenderNode for Bigger, Better Blurs"),
        NoteAbbr("8", "Compose for Wear OS 1.1 is now stable: check out new features!"),
        NoteAbbr("9", "Introducing the Architecture Templates"),
        NoteAbbr("10", "Android 13 for TV is now available"),
        NoteAbbr("11", "Blurring the Lines"),
        NoteAbbr("12", "Jetpack Compose — When should I use derivedStateOf?"),
        NoteAbbr("13", "Google Play Coffee break with Creatrip"),
        NoteAbbr("14", "RenderNode for Bigger, Better Blurs")
    )

    val folders = listOf(
        Folder("1", "Projects"),
        Folder("2", "Development"),
        Folder("3", "Business"),
        Folder("4", "Personal"),
        Folder("5", "Resources"),
        Folder("6", "Archive"),
    )

    val selectedFolder: MutableState<Folder?> = remember { mutableStateOf(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
                        NotesDrawerSheet(
                            folders = folders,
                            selectedFolder = selectedFolder.value
                        ) {
                            scope.launch { drawerState.close() }
                            selectedFolder.value = it
                        }
        },
        content = {
            Scaffold(
                topBar = {
                         TopAppBar(
                             title = { Text(
                                 text = selectedFolder.value?.name ?: stringResource(id = R.string.all_notes),
                                 style = MaterialTheme.typography.titleLarge,
                             ) },
                             navigationIcon = {
                                 IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                     Icon(Icons.Default.Menu, stringResource(id = R.string.desc_menu))
                                 }
                             },
                             actions = {
                                 IconButton(onClick = { /*TODO*/ }) {
                                     Icon(Icons.Default.Search, stringResource(id = R.string.desc_search))
                                 }
                                 IconButton(onClick = { /*TODO*/ }) {
                                     Icon(Icons.Default.MoreVert, stringResource(id = R.string.desc_more))
                                 }
                             }
                         )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(id = R.string.new_note)) },
                        icon = { Icon(Icons.Default.Edit, stringResource(id = R.string.new_note)) },
                        onClick = { navigateToNoteDetail("") })
                }
            ) { innerPadding ->
                NoteList(
                    notes = notes,
                    navigateToNoteDetail = navigateToNoteDetail,
                    paddingValues = innerPadding
                )
            }
        }
    )
}
