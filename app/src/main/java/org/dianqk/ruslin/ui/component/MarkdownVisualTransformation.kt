package org.dianqk.ruslin.ui.component

import android.util.Log
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

val HASHTAG_REGEX_PATTERN = Regex(pattern = "(#[A-Za-z0-9-_]+)(?:#[A-Za-z0-9-_]+)*")
val BOLD_REGEX_PATTERN = Regex(pattern = "(\\*{2})(\\s*\\b)([^\\*]*)(\\b\\s*)(\\*{2})")
val ITALICS_REGEX_PATTERN = Regex(pattern = "(\\~{2})(\\s*\\b)([^\\*]*)(\\b\\s*)(\\~{2})")
val HEADING_REGEX_PATTERN = Regex(pattern = "\\#{1,4}\\s([^\\#]*)\\s\\#{1,4}(?=\\n)")

class MarkdownVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        Log.d("MarkdownTF", "filter $text")
        val builder = AnnotatedString.Builder(text)
        val flavour = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(text.text)
        val visitor = AnnotatedStringGeneratingVisitor(builder, PROVIDERS)
        visitor.visitNode(parsedTree)
        return TransformedText(
            text = builder.toAnnotatedString(),
            offsetMapping = OffsetMapping.Identity
        )
    }

    inner class AnnotatedStringGeneratingVisitor(
        private val builder: AnnotatedString.Builder,
        private val providers: Map<IElementType, GeneratingProvider>
    ) : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            Log.d("MarkdownTF", "$node -> ${node.type}")
            providers[node.type]?.processNode(this, builder, node) ?: node.acceptChildren(this)
        }
    }
}

val PROVIDERS: Map<IElementType, GeneratingProvider> = hashMapOf(
    MarkdownElementTypes.STRONG to StrongGeneratingProvider(),
    MarkdownElementTypes.EMPH to EmphGeneratingProvider(),
    GFMElementTypes.STRIKETHROUGH to StrikethroughGeneratingProvider(),
    MarkdownElementTypes.ATX_1 to ATXHeaderGeneratingProvider(level = 1),
    MarkdownElementTypes.ATX_2 to ATXHeaderGeneratingProvider(level = 2),
    MarkdownElementTypes.ATX_3 to ATXHeaderGeneratingProvider(level = 3),
    MarkdownElementTypes.ATX_4 to ATXHeaderGeneratingProvider(level = 4),
    MarkdownElementTypes.ATX_5 to ATXHeaderGeneratingProvider(level = 5),
    MarkdownElementTypes.ATX_6 to ATXHeaderGeneratingProvider(level = 6)
)

interface GeneratingProvider {
    fun processNode(
        visitor: MarkdownVisualTransformation.AnnotatedStringGeneratingVisitor,
        builder: AnnotatedString.Builder,
        node: ASTNode
    )
}

class StrongGeneratingProvider : GeneratingProvider {
    override fun processNode(
        visitor: MarkdownVisualTransformation.AnnotatedStringGeneratingVisitor,
        builder: AnnotatedString.Builder,
        node: ASTNode
    ) {
        builder.addStyle(
            style = MarkdownDefaultTypography.bold.toSpanStyle(),
            node.startOffset,
            node.endOffset
        )
    }
}

class EmphGeneratingProvider : GeneratingProvider {
    override fun processNode(
        visitor: MarkdownVisualTransformation.AnnotatedStringGeneratingVisitor,
        builder: AnnotatedString.Builder,
        node: ASTNode
    ) {
        builder.addStyle(
            style = MarkdownDefaultTypography.emph.toSpanStyle(),
            node.startOffset,
            node.endOffset
        )
    }
}

class StrikethroughGeneratingProvider : GeneratingProvider {
    override fun processNode(
        visitor: MarkdownVisualTransformation.AnnotatedStringGeneratingVisitor,
        builder: AnnotatedString.Builder,
        node: ASTNode
    ) {
        builder.addStyle(
            style = MarkdownDefaultTypography.strikethrough.toSpanStyle(),
            node.startOffset,
            node.endOffset
        )
    }
}

class MarkdownTypography(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bold: TextStyle,
    val emph: TextStyle,
    val strikethrough: TextStyle
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
        textDecoration = TextDecoration.LineThrough
    )
)

class ATXHeaderGeneratingProvider(private val level: Int) : GeneratingProvider {
    override fun processNode(
        visitor: MarkdownVisualTransformation.AnnotatedStringGeneratingVisitor,
        builder: AnnotatedString.Builder,
        node: ASTNode
    ) {
        when (level) {
            1 -> builder.addStyle(
                style = MarkdownDefaultTypography.titleLarge.toSpanStyle(),
                node.startOffset,
                node.endOffset
            )
            2 -> builder.addStyle(
                style = MarkdownDefaultTypography.titleMedium.toSpanStyle(),
                node.startOffset,
                node.endOffset
            )
            else -> builder.addStyle(
                style = MarkdownDefaultTypography.titleSmall.toSpanStyle(),
                node.startOffset,
                node.endOffset
            )
        }
        node.acceptChildren(visitor)
    }
}

fun transformHeading(text: AnnotatedString): AnnotatedString {
    val matches = HEADING_REGEX_PATTERN.findAll(text.text)
    return if (matches.count() > 0) {
        Log.d("MarkdownTF", "match ${matches.count()}")
        val builder = AnnotatedString.Builder(text)
        for (match in matches) {
            val matchRange = match.range
            val headingLevel = getHeadingLevel(match.value)
            val sizeList = listOf(32.sp, 28.sp, 24.sp, 18.sp)
            builder.addStyle(
                style = SpanStyle(
                    color = Color.Gray,
                    baselineShift = BaselineShift.Superscript,
                    fontSize = sizeList[headingLevel - 1] / 4
                ),
                matchRange.first,
                matchRange.first + headingLevel
            )
            builder.addStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = sizeList[headingLevel - 1]
                ),
                matchRange.first + headingLevel,
                matchRange.last - headingLevel + 1
            )
            builder.addStyle(
                style = SpanStyle(
                    color = Color.Gray,
                    baselineShift = BaselineShift.Superscript,
                    fontSize = sizeList[headingLevel - 1] / 4
                ),
                matchRange.last - headingLevel + 1,
                matchRange.last + 1
            )
        }
        builder.toAnnotatedString()
    } else {
        text
    }
}

// fun transformItalics(text: AnnotatedString): Transformation {
//    val matches = ITALICS_REGEX_PATTERN.findAll(text.text)
//    return if (matches.count() > 0) {
//        val builder = AnnotatedString.Builder(text)
//        for (match in matches) {
//            val matchRange = match.range
//            builder.addStyle(
//                style = SpanStyle(color = Color.Gray, baselineShift = BaselineShift.Superscript, fontSize = 10.sp),
//                matchRange.first,
//                matchRange.first + 2
//            )
//            builder.addStyle(style = SpanStyle(fontStyle = FontStyle.Italic), matchRange.first + 2, matchRange.last - 1)
//            builder.addStyle(
//                style = SpanStyle(color = Color.Gray, baselineShift = BaselineShift.Superscript, fontSize = 10.sp),
//                matchRange.last - 1,
//                matchRange.last + 1
//            )
//        }
//        Transformation(annotatedString = builder.toAnnotatedString(), offsetMapping = OffsetMapping.Identity)
//    } else {
//        Transformation(annotatedString = text, offsetMapping = OffsetMapping.Identity)
//    }
// }
//
// fun transformBold(text: AnnotatedString): Transformation {
//    val matches = BOLD_REGEX_PATTERN.findAll(text.text)
//    return if (matches.count() > 0) {
//        val builder = AnnotatedString.Builder(text)
//        for (match in matches) {
//            val matchRange = match.range
//            builder.addStyle(
//                style = SpanStyle(color = Color.Gray, baselineShift = BaselineShift.Superscript, fontSize = 10.sp),
//                matchRange.first,
//                matchRange.first + 2
//            )
//            builder.addStyle(style = SpanStyle(fontWeight = FontWeight.Bold), matchRange.first + 2, matchRange.last - 1)
//            builder.addStyle(
//                style = SpanStyle(color = Color.Gray, baselineShift = BaselineShift.Superscript, fontSize = 10.sp),
//                matchRange.last - 1,
//                matchRange.last + 1
//            )
//        }
//        Transformation(annotatedString = builder.toAnnotatedString(), offsetMapping = OffsetMapping.Identity)
//    } else {
//        Transformation(annotatedString = text, offsetMapping = OffsetMapping.Identity)
//    }
// }
//
// fun transformHashtags(text: AnnotatedString): Transformation {
//    val builder = AnnotatedString.Builder(text)
//    val matches = HASHTAG_REGEX_PATTERN.findAll(text.text)
//    for (match in matches) {
//        val matchRange = match.range
//        builder.addStyle(style = SpanStyle(color = Color.Yellow), start = matchRange.first, end = matchRange.last + 1)
//    }
//    return Transformation(annotatedString = builder.toAnnotatedString(), offsetMapping = OffsetMapping.Identity)
// }

private fun getHeadingLevel(text: String): Int {
    var i = 0
    while (i < text.length) {
        if (text[i] == '#') {
            i++
        } else {
            break
        }
    }
    return i
}
