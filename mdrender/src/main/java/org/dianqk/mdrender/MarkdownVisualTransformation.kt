package org.dianqk.mdrender

import androidx.compose.material3.Typography
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

data class ParsedTree(
    internal val mdTree: ASTNode
)

class MarkdownVisualTransformation : VisualTransformation {

    fun parse(text: AnnotatedString): ParsedTree {
        val flavour = GFMFlavourDescriptor()
        return ParsedTree(MarkdownParser(flavour).buildMarkdownTreeFromString(text.text))
    }

    fun render(tree: ParsedTree, text: AnnotatedString): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        val visitor = AnnotatedStringGeneratingVisitor(builder, PROVIDERS)
        visitor.visitNode(tree.mdTree)
        return builder.toAnnotatedString()
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val parsedTree = parse(text)
        val renderText = render(parsedTree, text)
        return TransformedText(
            text = renderText,
            offsetMapping = OffsetMapping.Identity
        )
    }

    inner class AnnotatedStringGeneratingVisitor(
        private val builder: AnnotatedString.Builder,
        private val providers: Map<IElementType, GeneratingProvider>
    ) : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
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
