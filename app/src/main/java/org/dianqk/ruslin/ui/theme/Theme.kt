package org.dianqk.ruslin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.LocalDarkTheme
import org.dianqk.ruslin.data.LocalThemeIndex
import org.dianqk.ruslin.data.preference.DarkThemePreference
import org.dianqk.ruslin.ui.theme.palette.LocalTonalPalettes
import org.dianqk.ruslin.ui.theme.palette.TonalPalettes
import org.dianqk.ruslin.ui.theme.palette.core.ProvideZcamViewingConditions
import org.dianqk.ruslin.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper
import org.dianqk.ruslin.ui.theme.palette.dynamicDarkColorScheme
import org.dianqk.ruslin.ui.theme.palette.dynamicLightColorScheme


fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
fun RuslinTheme(
    wallpaperPalettes: List<TonalPalettes> = extractTonalPalettesFromUserWallpaper(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = LocalDarkTheme.current
    val themeIndex = LocalThemeIndex.current
    val useDarkTheme = darkTheme.isDarkTheme()

    val tonalPalettes = wallpaperPalettes[
            if (themeIndex >= wallpaperPalettes.size) {
                0
            } else {
                themeIndex
            }
    ]

    rememberSystemUiController().run {
        setStatusBarColor(Color.Transparent, !useDarkTheme)
        setSystemBarsColor(Color.Transparent, !useDarkTheme)
        setNavigationBarColor(Color.Transparent, !useDarkTheme)
    }

    LaunchedEffect(darkTheme) {
        context.setTheme(
            when (darkTheme) {
                DarkThemePreference.UseDeviceTheme -> R.style.Theme_Ruslin
                DarkThemePreference.ON -> R.style.Theme_Ruslin_Dark
                DarkThemePreference.OFF -> R.style.Theme_Ruslin_Light
            }
        )
    }

    ProvideZcamViewingConditions {
        CompositionLocalProvider(
            LocalTonalPalettes provides tonalPalettes.apply { Preparing() },
        ) {
            MaterialTheme(
                colorScheme = if (darkTheme.isDarkTheme()) dynamicDarkColorScheme() else dynamicLightColorScheme(),
                shapes = Shapes,
                typography = Typography,
                content = content,
            )
        }
    }
}
