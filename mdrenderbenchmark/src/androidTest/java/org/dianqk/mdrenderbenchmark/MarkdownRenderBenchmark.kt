package org.dianqk.mdrenderbenchmark

import android.content.Context
import androidx.annotation.RawRes
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.dianqk.mdrender.MarkdownVisualTransformation
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testing markdown parsing and rendering performance.
 *
 * You man need to disable the root privileges of the shell(\[SharedUID\] Shell) in Magisk to avoid getting stuck in the test boot session.
 */
@RunWith(AndroidJUnit4::class)
class MarkdownRenderBenchmark {

    private lateinit var instrumentationContext: Context

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun benchmarkText1Filter() {
        // https://raw.githubusercontent.com/markdown-it/markdown-it/df4607f1d4d4be7fdc32e71c04109aea8cc373fa/support/demo_template/sample.md
        val text = readText(R.raw.text1)
        val markdownVisualTransformation = MarkdownVisualTransformation()
        benchmarkRule.measureRepeated {
            markdownVisualTransformation.invalid()
            markdownVisualTransformation.filter(text)
        }
    }

    @Test
    fun benchmarkText1Parse() {
        val text = readText(R.raw.text1)
        val markdownVisualTransformation = MarkdownVisualTransformation()
        benchmarkRule.measureRepeated {
            markdownVisualTransformation.parse(text)
        }
    }

    @Test
    fun benchmarkText1Render() {
        val text = readText(R.raw.text1)
        val markdownVisualTransformation = MarkdownVisualTransformation()
        val parsedTagRanges = markdownVisualTransformation.parse(text)
        benchmarkRule.measureRepeated {
            markdownVisualTransformation.render(parsedTagRanges, text)
        }
    }

    private fun readText(@RawRes id: Int): AnnotatedString {
        val textInputStream = instrumentationContext.resources.openRawResource(id)
        val text = textInputStream.bufferedReader().use { it.readText() }
        return AnnotatedString(text)
    }
}