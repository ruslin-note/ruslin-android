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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.ui.ext.formatAsYmdHms
import org.dianqk.ruslin.ui.theme.RuslinTheme
import uniffi.ruslin.FfiAbbrNote
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun NoteList(
    notes: List<FfiAbbrNote>,
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
fun NoteAbbrCard(note: FfiAbbrNote, navigateToNoteDetail: (String) -> Unit) {
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

        Text(text = Date(note.updatedTime).formatAsYmdHms(LocalContext.current), style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun NoteListPreview() {
    RuslinTheme {
//        val notes = emptyList()
//        NoteList(notes = notes, navigateToNoteDetail = {}, paddingValues = PaddingValues(0.dp))
    }
}