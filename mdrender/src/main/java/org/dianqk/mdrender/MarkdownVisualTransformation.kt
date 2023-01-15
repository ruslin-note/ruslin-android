package org.dianqk.mdrender

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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

class MarkdownVisualTransformation(private val colorScheme: ColorScheme) : VisualTransformation {

    private var cacheRenderText: AnnotatedString? = null

    fun invalid() {
        cacheRenderText = null
    }

    fun parse(text: AnnotatedString): ParsedTagRanges {
        val markdownTagRanges = parseMarkdown(text.text)
        return ParsedTagRanges(markdownTagRanges)
    }

    fun render(tree: ParsedTagRanges, text: AnnotatedString): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        for (tagRange in tree.markdownTagRanges) {
            when (tagRange) {
                is MarkdownTagRange.Heading -> tagRange.render(builder, colorScheme)
                is MarkdownTagRange.Emphasis -> tagRange.render(builder, colorScheme)
                is MarkdownTagRange.Strong -> tagRange.render(builder, colorScheme)
                is MarkdownTagRange.Strikethrough -> tagRange.render(builder)
                is MarkdownTagRange.InlineCode -> tagRange.render(builder, colorScheme)
            }
        }
        return builder.toAnnotatedString()
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val currentCacheRenderText = cacheRenderText
        if (currentCacheRenderText != null) {
            return TransformedText(
                text = currentCacheRenderText,
                offsetMapping = OffsetMapping.Identity
            )
        }
        val parsedTree = parse(text)
        val renderText = render(parsedTree, text)
        cacheRenderText = renderText
        return TransformedText(
            text = renderText,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

class MarkdownTypography(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bold: TextStyle,
    val emph: TextStyle,
    val strikethrough: TextStyle,
    val inlineCode: TextStyle,
)

var DefaultTypography = Typography()

val MarkdownDefaultTypography = MarkdownTypography(
    titleLarge = TextStyle(
        fontFamily = DefaultTypography.titleLarge.fontFamily,
        fontWeight = DefaultTypography.titleLarge.fontWeight,
        fontSize = DefaultTypography.titleLarge.fontSize,
        lineHeight = DefaultTypography.titleLarge.lineHeight,
        letterSpacing = DefaultTypography.titleLarge.letterSpacing
    ),
    titleMedium = TextStyle(
        fontFamily = DefaultTypography.titleMedium.fontFamily,
        fontWeight = DefaultTypography.titleMedium.fontWeight,
        fontSize = DefaultTypography.titleMedium.fontSize,
        lineHeight = DefaultTypography.titleMedium.lineHeight,
        letterSpacing = DefaultTypography.titleMedium.letterSpacing
    ),
    titleSmall = TextStyle(
        fontFamily = DefaultTypography.titleSmall.fontFamily,
        fontWeight = DefaultTypography.titleSmall.fontWeight,
        fontSize = DefaultTypography.titleSmall.fontSize,
        lineHeight = DefaultTypography.titleSmall.lineHeight,
        letterSpacing = DefaultTypography.titleSmall.letterSpacing
    ),
    bold = TextStyle(
        fontWeight = FontWeight.Bold
    ),
    emph = TextStyle(
        fontStyle = FontStyle.Italic
    ),
    strikethrough = TextStyle(
        color = Color.Black.copy(alpha = 0.5f),
        textDecoration = TextDecoration.LineThrough
    ),
    inlineCode = TextStyle(
        background = Color.LightGray.copy(alpha = 0.5f),
    )
)

private fun MarkdownTagRange.Heading.render(
    builder: AnnotatedString.Builder,
    colorScheme: ColorScheme
) {
    builder.addStyle(SpanStyle(color = colorScheme.primary), start, start + level)
    val style = when (level) {
        1 -> MarkdownDefaultTypography.titleLarge.toSpanStyle()
        2 -> MarkdownDefaultTypography.titleMedium.toSpanStyle()
        else -> MarkdownDefaultTypography.titleSmall.toSpanStyle()
    }
    builder.addStyle(
        style,
        start,
        end
    )
}

private fun MarkdownTagRange.Emphasis.render(
    builder: AnnotatedString.Builder,
    colorScheme: ColorScheme
) {
    builder.addStyle(SpanStyle(color = colorScheme.primary), start, start + 1)
    builder.addStyle(SpanStyle(color = colorScheme.primary), end - 1, end)
    builder.addStyle(
        MarkdownDefaultTypography.emph.toSpanStyle(),
        start,
        end
    )
}

private fun MarkdownTagRange.Strong.render(
    builder: AnnotatedString.Builder,
    colorScheme: ColorScheme
) {
    builder.addStyle(SpanStyle(color = colorScheme.primary), start, start + 2)
    builder.addStyle(SpanStyle(color = colorScheme.primary), end - 2, end)
    builder.addStyle(
        MarkdownDefaultTypography.bold.toSpanStyle(),
        start,
        end
    )
}

private fun MarkdownTagRange.Strikethrough.render(builder: AnnotatedString.Builder) {
    builder.addStyle(
        MarkdownDefaultTypography.strikethrough.toSpanStyle(),
        start,
        end
    )
}

private fun MarkdownTagRange.InlineCode.render(
    builder: AnnotatedString.Builder,
    colorScheme: ColorScheme
) {
    builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start + 1, end - 1)
    builder.addStyle(SpanStyle(color = colorScheme.primary), start, start + 1)
    builder.addStyle(SpanStyle(color = colorScheme.primary), end - 1, end)
    builder.addStyle(
        MarkdownDefaultTypography.inlineCode.toSpanStyle(),
        start,
        end
    )
}
