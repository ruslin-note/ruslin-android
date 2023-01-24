package org.dianqk.ruslin.ui.component

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.dianqk.mdrender.MarkdownVisualTransformation
import org.dianqk.ruslin.R
import uniffi.ruslin.FfiResource
import java.io.FileOutputStream
import java.io.OutputStream

sealed class MarkdownInsertTagType {
    class Heading(val level: Int) : MarkdownInsertTagType()
    object Bold : MarkdownInsertTagType()
    object Italic : MarkdownInsertTagType()
    object ListBulleted : MarkdownInsertTagType()
    object ListNumbered : MarkdownInsertTagType()

    object Strikethrough : MarkdownInsertTagType()

    object Quote : MarkdownInsertTagType()

}

fun MarkdownInsertTagType.Heading.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, " ")
    builder.insert(textFieldValue.selection.end, "#".repeat(level))
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + level + 1,
            end = textFieldValue.selection.end + level + 1,
        )
    )
}

fun MarkdownInsertTagType.Bold.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "**")
    builder.insert(textFieldValue.selection.start, "**")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 2,
            end = textFieldValue.selection.end + 2,
        )
    )
}

fun MarkdownInsertTagType.Italic.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "*")
    builder.insert(textFieldValue.selection.start, "*")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 1,
            end = textFieldValue.selection.end + 1,
        )
    )
}

fun MarkdownInsertTagType.ListBulleted.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "- ")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 2,
            end = textFieldValue.selection.end + 2,
        )
    )
}

fun MarkdownInsertTagType.ListNumbered.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "1. ")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 3,
            end = textFieldValue.selection.end + 3,
        )
    )
}

fun MarkdownInsertTagType.Strikethrough.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "~~")
    builder.insert(textFieldValue.selection.start, "~~")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 2,
            end = textFieldValue.selection.end + 2,
        )
    )
}

fun MarkdownInsertTagType.Quote.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    builder.insert(textFieldValue.selection.end, "> ")
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + 2,
            end = textFieldValue.selection.end + 2,
        )
    )
}

@Composable
fun EditorToolbar(
    modifier: Modifier = Modifier,
    createFfiResource: () -> FfiResource,
    onInsertMarkdownTag: (MarkdownInsertTagType) -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
            result?.let { uri: Uri ->
                val mime = context.contentResolver.getType(uri)
                context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
                    val resource = createFfiResource()
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    resource.filename = cursor.getString(nameIndex)
                    resource.size = cursor.getInt(sizeIndex)
                    val resourceDir = context.filesDir.resolve("resource")
                    resourceDir.mkdirs()
                    val resourceFile = resourceDir.resolve(resource.filename)
                    resourceFile.createNewFile()
                    val inputStream = context.contentResolver.openInputStream(uri)!!
                    val outputStream: OutputStream = FileOutputStream(resourceFile)
                    val buf = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                        buf,
                        0,
                        len
                    )
                    outputStream.close()
                    inputStream.close()
                    cursor.close()
                }
            }
        }

    Row(
        modifier = modifier
            .padding(horizontal = 6.dp)
            .horizontalScroll(rememberScrollState())
    ) {

        Box(modifier = Modifier) {
            IconButton(
                enabled = !expanded,
                onClick = { expanded = true }) {
                Icon(Icons.Default.Title, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                properties = PopupProperties(focusable = false),
                onDismissRequest = {
                    expanded = false
                }
            ) {
                Row() {
                    HeadingDropdownMenuItem(6, R.drawable.format_h6) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                    HeadingDropdownMenuItem(5, R.drawable.format_h5) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                    HeadingDropdownMenuItem(4, R.drawable.format_h4) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                    HeadingDropdownMenuItem(3, R.drawable.format_h3) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                    HeadingDropdownMenuItem(2, R.drawable.format_h2) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                    HeadingDropdownMenuItem(1, R.drawable.format_h1) {
                        onInsertMarkdownTag(it)
                        expanded = false
                    }
                }

            }
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.Bold) }) {
            Icon(Icons.Default.FormatBold, contentDescription = null)
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.Italic) }) {
            Icon(Icons.Default.FormatItalic, contentDescription = null)
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.ListBulleted) }) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = null)
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.ListNumbered) }) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = null)
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.Strikethrough) }) {
            Icon(Icons.Default.FormatStrikethrough, contentDescription = null)
        }
        IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.Quote) }) {
            Icon(Icons.Default.FormatQuote, contentDescription = null)
        }
        IconButton(onClick = {
            launcher.launch("*/*")
        }) {
            Icon(Icons.Default.Image, contentDescription = null)
        }
    }
}


@Composable
fun HeadingDropdownMenuItem(
    level: Int,
    @DrawableRes resourceId: Int,
    onInsertMarkdownTag: (MarkdownInsertTagType.Heading) -> Unit
) {
    IconButton(onClick = { onInsertMarkdownTag(MarkdownInsertTagType.Heading(level = level)) }) {
        Icon(ImageVector.vectorResource(id = resourceId), contentDescription = null)
    }
}
