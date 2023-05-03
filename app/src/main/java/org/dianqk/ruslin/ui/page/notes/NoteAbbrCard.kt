package org.dianqk.ruslin.ui.page.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.ui.ext.formatAsYmdHms
import uniffi.ruslin.FfiAbbrNote
import java.util.Date

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NoteAbbrCard(
    modifier: Modifier = Modifier,
    note: FfiAbbrNote,
    titleTextDirection: TextDirection,
    isSelectEnabled: () -> Boolean,
    isSelected: () -> Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(modifier = with(modifier) {
        if (isSelectEnabled()) {
            selectable(selected = isSelected(), onClick = onSelect)
        } else {
            combinedClickable(
                enabled = true,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }
    }
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(modifier = Modifier) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterVertically),
                visible = isSelectEnabled(),
            ) {
                Checkbox(
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                    checked = isSelected(),
                    onCheckedChange = null
                )
            }
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(textDirection = titleTextDirection),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Date(note.userUpdatedTime).formatAsYmdHms(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}