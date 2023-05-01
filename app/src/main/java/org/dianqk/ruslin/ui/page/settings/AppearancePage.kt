package org.dianqk.ruslin.ui.page.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.dianqk.mdrender.MarkdownTheme
import org.dianqk.mdrender.MarkdownVisualTransformation
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.LocalDarkTheme
import org.dianqk.ruslin.data.LocalThemeIndex
import org.dianqk.ruslin.data.preference.ThemeIndexPreference
import org.dianqk.ruslin.data.preference.not
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.PreferenceSwitchWithDivider
import org.dianqk.ruslin.ui.theme.palette.TonalPalettes
import org.dianqk.ruslin.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePage(
    navigateToDarkTheme: () -> Unit,
    onPopBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeIndex = LocalThemeIndex.current
    val darkTheme = LocalDarkTheme.current
    val darkThemeNot = !darkTheme

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val wallpaperTonalPalettes = extractTonalPalettesFromUserWallpaper()
    val previewText = """
        # Heading level 1
        ## Heading level 2
        ### Heading level 3
        **bold** *italic* `inline code`
        - Unordered Item
        - Unordered Item
        ```
        fn main() {
            println!("Hello World!");
        }
        ```
    """.trimIndent()
    val colorScheme = MaterialTheme.colorScheme
    val visualTransformation =
        remember(colorScheme.onBackground) {
            MarkdownVisualTransformation(
                MarkdownTheme.from(
                    colorScheme = colorScheme,
                    contentColor = colorScheme.onBackground
                )
            )
        }
    val previewMarkdownText = visualTransformation.filter(AnnotatedString(previewText))

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.color_and_style),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.color_and_style),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 0.dp,
                        end = 24.dp,
                        bottom = 6.dp
                    ),
                    text = previewMarkdownText.text
                )
            }
            item {
                Palettes(
                    context = context,
                    themeIndex = themeIndex,
                    palettes = wallpaperTonalPalettes,
                )
            }
            item {
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    description = darkTheme.toDesc(context),
                    isChecked = darkTheme.isDarkTheme(),
                    onClick = navigateToDarkTheme,
                    onChecked = {
                        darkThemeNot.put(context = context, scope = scope)
                    }
                )
            }
        }
    }
}

@Composable
fun Palettes(
    context: Context,
    palettes: List<TonalPalettes>,
    themeIndex: Int = 0,
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palettes.forEachIndexed { index, palette ->
            SelectableMiniPalette(
                selected = themeIndex == index,
                onClick = {
                    ThemeIndexPreference.put(context, scope, index)
                },
                palette = palette
            )
        }
    }

}

@Composable
fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    palette: TonalPalettes,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color =
        MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Surface(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            color = palette primary 90,
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .offset((-24).dp, 24.dp),
                    color = palette tertiary 90,
                ) {}
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .offset(24.dp, 24.dp),
                    color = palette secondary 60,
                ) {}
                AnimatedVisibility(
                    visible = selected,
                    modifier = Modifier
                        .clip(CircleShape)
                        .align(Alignment.Center),
                    enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                    exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}