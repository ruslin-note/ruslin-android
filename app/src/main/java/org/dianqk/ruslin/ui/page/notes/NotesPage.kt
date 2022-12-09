package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.theme.RuslinTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(
    navigateToNoteDetail: (String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(items[0]) }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item, contentDescription = null) },
                        label = { Text(item.name) },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                         TopAppBar(
                             title = { Text(text = stringResource(id = R.string.app_name)) },
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

data class NoteAbbr(
    val id: String,
    val title: String,
    val date: String = "2022/01/01 10:00:00",
)

@Composable
fun NoteList(
    notes: List<NoteAbbr>,
    navigateToNoteDetail: (String) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteAbbrCard(note = note, navigateToNoteDetail = navigateToNoteDetail)
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    }
}

@Composable
fun NoteAbbrCard(note: NoteAbbr, navigateToNoteDetail: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navigateToNoteDetail(note.id) })
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ,
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = note.date, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun NoteListPreview() {
    RuslinTheme {
        val notes = listOf<NoteAbbr>(
            NoteAbbr("1", "Compose for Wear OS 1.1 is now stable: check out new features!"),
            NoteAbbr("2", "Introducing the Architecture Templates"),
            NoteAbbr("3", "Android 13 for TV is now available"),
            NoteAbbr("4", "Blurring the Lines"),
            NoteAbbr("5", "Jetpack Compose — When should I use derivedStateOf?"),
            NoteAbbr("6", "Google Play Coffee break with Creatrip"),
            NoteAbbr("7", "RenderNode for Bigger, Better Blurs")
        )
        NoteList(notes = notes, navigateToNoteDetail = {}, paddingValues = PaddingValues(0.dp))
    }
}