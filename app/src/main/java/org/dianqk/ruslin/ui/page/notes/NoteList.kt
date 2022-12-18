package org.dianqk.ruslin.ui.page.notes

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dianqk.ruslin.ui.component.BottomDrawer
import org.dianqk.ruslin.ui.ext.formatAsYmdHms
import org.dianqk.ruslin.ui.theme.RuslinTheme
import uniffi.ruslin.FfiAbbrNote
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun NoteList(
    modifier: Modifier = Modifier,
    notes: List<FfiAbbrNote>,
    navigateToNoteDetail: (String) -> Unit,
    showActionBottomDrawer: (FfiAbbrNote) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        items(notes, key = { it.id }) { note ->
            NoteAbbrCard(note = note, navigateToNoteDetail = navigateToNoteDetail, showActionBottomDrawer)
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NoteAbbrCard(
    note: FfiAbbrNote,
    navigateToNoteDetail: (String) -> Unit,
    showActionBottomDrawer: (FfiAbbrNote) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { navigateToNoteDetail(note.id) },
                onLongClick = { showActionBottomDrawer(note) },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
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