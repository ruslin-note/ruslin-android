package org.dianqk.mdrender

import android.util.Range
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import uniffi.ruslin.MarkdownTagRange
import uniffi.ruslin.parseMarkdown

data class ParsedTagRanges(
    internal val markdownTagRanges: List<MarkdownTagRange>
)

class MarkdownVisualTransformation(private val theme: MarkdownTheme = MarkdownDefaultTheme) :
    VisualTransformation {

    private var cachedRenderText: AnnotatedString? = null
    var cachedParsedTagRanges: ParsedTagRanges = ParsedTagRanges(emptyList())
        private set

    var unorderedListRanges: MutableList<Range<Int>> = mutableListOf()
    var orderedListRanges: MutableList<Range<Int>> = mutableListOf()

    fun invalid() {
        cachedRenderText = null
        cachedParsedTagRanges = ParsedTagRanges(emptyList())
        unorderedListRanges.clear()
        orderedListRanges.clear()
    }

    fun parse(text: AnnotatedString): ParsedTagRanges {
        val markdownTagRanges = parseMarkdown(text.text)
        return ParsedTagRanges(markdownTagRanges)
    }

    fun render(tree: ParsedTagRanges, text: AnnotatedString): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        builder.addStyle(theme.contentStyle, 0, builder.length)
        for (tagRange in tree.markdownTagRanges) {
            when (tagRange) {
                is MarkdownTagRange.Heading -> tagRange.render(builder, theme)
                is MarkdownTagRange.Emphasis -> tagRange.render(builder, theme)
                is MarkdownTagRange.Strong -> tagRange.render(builder, theme)
                is MarkdownTagRange.Strikethrough -> tagRange.render(builder, theme)
                is MarkdownTagRange.InlineCode -> tagRange.render(builder, theme)
                is MarkdownTagRange.ListItem -> tagRange.render(builder, theme)
                is MarkdownTagRange.MList -> tagRange.render(this)
                is MarkdownTagRange.Paragraph -> {}
                is MarkdownTagRange.Link -> tagRange.render(builder, theme)
                is MarkdownTagRange.Image -> tagRange.render(builder, theme)
                is MarkdownTagRange.Rule -> tagRange.render(builder, theme)
                is MarkdownTagRange.BlockQuote -> tagRange.render(builder, theme)
                is MarkdownTagRange.TaskListMarker -> tagRange.render(builder, theme)
                is MarkdownTagRange.CodeBlock -> tagRange.render(builder, theme)
            }
        }
        return builder.toAnnotatedString()
    }

    fun matchTag(
        index: Int,
        onMatchUnOrderList: () -> Unit,
        onMatchOrderList: () -> Unit
    ) {
        for (unorderedListRange in unorderedListRanges) {
            if (unorderedListRange.contains(index)) {
                onMatchUnOrderList()
                return
            }
        }
        for (orderedListRange in orderedListRanges) {
            if (orderedListRange.contains(index)) {
                onMatchOrderList()
                return
            }
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        if (text.isEmpty()) {
            return TransformedText(text = text, offsetMapping = OffsetMapping.Identity)
        }
        val currentCachedRenderText = cachedRenderText
        if (currentCachedRenderText != null) {
            return TransformedText(
                text = currentCachedRenderText,
                offsetMapping = OffsetMapping.Identity
            )
        }
        val parsedTagRanges = parse(text)
        cachedParsedTagRanges = parsedTagRanges
        val renderText = render(parsedTagRanges, text)
        cachedRenderText = renderText
        return TransformedText(
            text = renderText,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

var DefaultTypography = Typography()

val MarkdownDefaultTheme = MarkdownTheme()

data class MarkdownTheme(
    val contentStyle: SpanStyle = SpanStyle(),
    val titleLarge: SpanStyle = SpanStyle(
        fontFamily = DefaultTypography.titleLarge.fontFamily,
        fontWeight = DefaultTypography.titleLarge.fontWeight,
        fontSize = DefaultTypography.titleLarge.fontSize,
        letterSpacing = DefaultTypography.titleLarge.letterSpacing
    ),
    val titleMedium: SpanStyle = SpanStyle(
        fontFamily = DefaultTypography.titleMedium.fontFamily,
        fontWeight = DefaultTypography.titleMedium.fontWeight,
        fontSize = DefaultTypography.titleMedium.fontSize,
        letterSpacing = DefaultTypography.titleMedium.letterSpacing
    ),
    val titleSmall: SpanStyle = SpanStyle(
        fontFamily = DefaultTypography.titleSmall.fontFamily,
        fontWeight = DefaultTypography.titleSmall.fontWeight,
        fontSize = DefaultTypography.titleSmall.fontSize,
        letterSpacing = DefaultTypography.titleSmall.letterSpacing
    ),
    val titleTag: SpanStyle = SpanStyle(),
    val bold: SpanStyle = SpanStyle(
        fontWeight = FontWeight.Bold
    ),
    val boldTag: SpanStyle = SpanStyle(),
    val emph: SpanStyle = SpanStyle(
        fontStyle = FontStyle.Italic
    ),
    val emphTag: SpanStyle = SpanStyle(),
    val strikethrough: SpanStyle = SpanStyle(
        textDecoration = TextDecoration.LineThrough
    ),
    val strikethroughTag: SpanStyle = SpanStyle(),
    val inlineCode: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
    ),
    val inlineCodeTag: SpanStyle = SpanStyle(),
    val listTag: SpanStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
    ),
    val linkTag: SpanStyle = SpanStyle(),
    val imageTag: SpanStyle = SpanStyle(),
    val ruleTag: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace),
    val blockQuoteTag: SpanStyle = SpanStyle(),
    val blockQuote: SpanStyle = SpanStyle(),
    val taskListMarkerTag: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace),
    val codeBlock: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace)
) {

    companion object {
        fun from(colorScheme: ColorScheme, contentColor: Color): MarkdownTheme = MarkdownDefaultTheme.copy(
            contentStyle = SpanStyle(color = contentColor),
            titleTag = MarkdownDefaultTheme.titleTag.copy(color = colorScheme.primary),
            boldTag = MarkdownDefaultTheme.boldTag.copy(color = colorScheme.primary),
            emphTag = MarkdownDefaultTheme.emphTag.copy(color = colorScheme.primary),
            strikethrough = MarkdownDefaultTheme.strikethrough.copy(color = Color.Gray),
            strikethroughTag = MarkdownDefaultTheme.strikethroughTag.copy(color = Color.Gray),
            inlineCodeTag = MarkdownDefaultTheme.inlineCodeTag.copy(color = colorScheme.primary),
            inlineCode = MarkdownDefaultTheme.inlineCode.copy(color = colorScheme.primary),
            listTag = MarkdownDefaultTheme.listTag.copy(color = colorScheme.primary),
            linkTag = MarkdownDefaultTheme.linkTag.copy(color = colorScheme.primary),
            imageTag = MarkdownDefaultTheme.imageTag.copy(color = colorScheme.primary),
            ruleTag = MarkdownDefaultTheme.ruleTag.copy(color = colorScheme.primary),
            blockQuoteTag = MarkdownDefaultTheme.blockQuoteTag.copy(color = colorScheme.primary),
            taskListMarkerTag = MarkdownDefaultTheme.taskListMarkerTag.copy(color = colorScheme.primary),
            codeBlock = MarkdownDefaultTheme.codeBlock.copy(color = colorScheme.primary)
        )
    }
}

private fun MarkdownTagRange.Heading.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    val style = when (level) {
        1 -> MarkdownDefaultTheme.titleLarge
        2 -> MarkdownDefaultTheme.titleMedium
        else -> MarkdownDefaultTheme.titleSmall
    }
    builder.addStyle(
        style,
        start,
        end
    )
    builder.addStyle(theme.titleTag, start, start + level)
}

private fun MarkdownTagRange.Emphasis.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        MarkdownDefaultTheme.emph,
        start,
        end
    )
    builder.addStyle(theme.emphTag, start, start + 1)
    builder.addStyle(theme.emphTag, end - 1, end)
}

private fun MarkdownTagRange.Strong.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        MarkdownDefaultTheme.bold,
        start,
        end
    )
    builder.addStyle(theme.boldTag, start, start + 2)
    builder.addStyle(theme.boldTag, end - 2, end)
}

private fun MarkdownTagRange.Strikethrough.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        theme.strikethrough,
        start,
        end
    )
    builder.addStyle(theme.strikethroughTag, start, start + 2)
    builder.addStyle(theme.strikethroughTag, end - 2, end)
}

private fun MarkdownTagRange.InlineCode.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(theme.inlineCode, start, end)
    builder.addStyle(theme.inlineCodeTag, start, start + 1)
    builder.addStyle(theme.inlineCodeTag, end - 1, end)
}

private fun MarkdownTagRange.ListItem.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        theme.listTag,
        start = start,
        end = kotlin.math.min(start + (if (ordered) 3 else 2), end)
    )
//    debug
//    builder.addStyle(SpanStyle(background = Color.Black.copy(alpha = 0.3f)), start = start + (if (ordered) 3 else 2), end = end)
}

private fun MarkdownTagRange.MList.render(
    transformation: MarkdownVisualTransformation,
) {
    if (order == 0) {
        transformation.unorderedListRanges.add(Range(start, end))
    } else {
        transformation.orderedListRanges.add(Range(start, end))
    }
}

private fun MarkdownTagRange.Link.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    // []
    builder.addStyle(theme.linkTag, start, start + 1)
    builder.addStyle(theme.linkTag, urlOffset - 2, urlOffset - 1)

    // ()
    builder.addStyle(theme.linkTag, urlOffset - 1, urlOffset)
    builder.addStyle(theme.linkTag, end - 1, end)

    // url
//    builder.addStyle(SpanStyle(color = colorScheme.primary), urlOffset, end - 1);
}

private fun MarkdownTagRange.Image.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    // []
    builder.addStyle(theme.imageTag, start, start + 2)
    builder.addStyle(theme.imageTag, urlOffset - 2, urlOffset - 1)

    // ()
    builder.addStyle(theme.imageTag, urlOffset - 1, urlOffset)
    builder.addStyle(theme.imageTag, end - 1, end)

    // url
    builder.addStyle(theme.imageTag, urlOffset, end - 1)
}

private fun MarkdownTagRange.Rule.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        theme.ruleTag,
        start,
        end
    )
}

private fun MarkdownTagRange.BlockQuote.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(theme.blockQuote, start, end)
    builder.addStyle(
        theme.blockQuoteTag,
        start,
        start + 1
    )
}

private fun MarkdownTagRange.TaskListMarker.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        theme.taskListMarkerTag,
        start,
        end
    )
}

private fun MarkdownTagRange.CodeBlock.render(
    builder: AnnotatedString.Builder,
    theme: MarkdownTheme
) {
    builder.addStyle(
        theme.codeBlock,
        start,
        end
    )
}
