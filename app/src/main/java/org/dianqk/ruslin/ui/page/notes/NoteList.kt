package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.ui.theme.RuslinTheme


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