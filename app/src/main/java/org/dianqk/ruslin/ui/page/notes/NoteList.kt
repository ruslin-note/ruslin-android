package org.dianqk.ruslin.ui.page.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.ContentEmptyState
import org.dianqk.ruslin.ui.ext.formatAsYmdHms
import org.dianqk.ruslin.ui.theme.RuslinTheme
import uniffi.ruslin.FfiAbbrNote
import java.util.*

@Composable
fun NoteList(
    modifier: Modifier = Modifier,
    notes: List<FfiAbbrNote>,
    navigateToNote: (String) -> Unit,
    showActionBottomDrawer: (FfiAbbrNote) -> Unit
) {
    if (notes.isEmpty()) {
        ContentEmptyState {
            Text(text = stringResource(id = R.string.create_a_note))
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
        ) {
            items(notes, key = { it.id }) { note ->
                NoteAbbrCard(
                    note = note,
                    navigateToNote = navigateToNote,
                    showActionBottomDrawer
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NoteAbbrCard(
    note: FfiAbbrNote,
    navigateToNote: (String) -> Unit,
    showActionBottomDrawer: (FfiAbbrNote) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { navigateToNote(note.id) },
                onLongClick = { showActionBottomDrawer(note) }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = Date(note.updatedTime).formatAsYmdHms(LocalContext.current),
            style = MaterialTheme.typography.bodySmall
        )
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
