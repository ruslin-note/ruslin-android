package org.dianqk.ruslin.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.dianqk.mdrender.MarkdownVisualTransformation
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.page.note_detail.SavedResource

sealed class MarkdownInsertTagType {
    class Heading(val level: Int) : MarkdownInsertTagType()
    object Bold : MarkdownInsertTagType()
    object Italic : MarkdownInsertTagType()
    object ListBulleted : MarkdownInsertTagType()
    object ListNumbered : MarkdownInsertTagType()

    object Strikethrough : MarkdownInsertTagType()

    object Quote : MarkdownInsertTagType()

    class Image(val resourceId: String, val filename: String, val isImage: Boolean) :
        MarkdownInsertTagType()

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

fun MarkdownInsertTagType.Image.insert(
    textFieldValue: TextFieldValue,
    markdownVisualTransformation: MarkdownVisualTransformation
): TextFieldValue {
    val text = textFieldValue.text
    val builder = StringBuilder(text)
    val urlTagBuilder = StringBuilder(resourceId.length + filename.length + 7)
    if (isImage) {
        urlTagBuilder.append("!")
    }
    urlTagBuilder.append("[").append(filename).append("](:/").append(resourceId).append(")")
    val urlTag = urlTagBuilder.toString()
    builder.insert(textFieldValue.selection.end, urlTag)
    return textFieldValue.copy(
        text = builder.toString(),
        selection = TextRange(
            start = textFieldValue.selection.start + urlTag.length,
            end = textFieldValue.selection.end + urlTag.length,
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorToolbar(
    modifier: Modifier = Modifier,
    onSaveResource: (Uri) -> SavedResource?,
    onInsertMarkdownTag: (MarkdownInsertTagType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val imeTargetBottom = WindowInsets.imeAnimationTarget.getBottom(density = density)
    val isImeVisible = imeTargetBottom != 0
//    val isImeVisible = WindowInsets.isImeVisible

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
            result?.let { uri: Uri ->
                onSaveResource(uri)?.apply {
                    onInsertMarkdownTag(
                        MarkdownInsertTagType.Image(
                            resourceId = id,
                            filename = filename,
                            isImage = isImage
                        )
                    )
                }
            }
        }

    if (isImeVisible) {
        Divider(modifier = Modifier.fillMaxWidth())
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
