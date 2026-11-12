package com.aleespa.randomsquare.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette
import com.aleespa.randomsquare.data.AppThemeSource
import com.aleespa.randomsquare.data.SettingDarkMode

// Every role is set explicitly: any role left out falls back to Material's baseline purple palette,
// which used to leak into buttons, the navigation indicator and dialogs.

val DarkColorScheme = darkColorScheme(
    primary = Color(132, 158, 190),
    onPrimary = Color(4, 30, 58),
    primaryContainer = Color(38, 61, 91),
    onPrimaryContainer = Color(210, 225, 245),
    inversePrimary = Color(52, 84, 122),
    secondary = Color(91, 120, 157),
    onSecondary = Color(0, 20, 42),
    secondaryContainer = Color(26, 44, 68),
    onSecondaryContainer = Color(200, 216, 236),
    tertiary = Color(38, 61, 91),
    onTertiary = Color(220, 230, 245),
    tertiaryContainer = Color(20, 36, 58),
    onTertiaryContainer = Color(200, 216, 236),
    background = Color(0, 6, 15),
    onBackground = Color(226, 232, 240),
    surface = Color(0, 13, 30),
    onSurface = Color(226, 232, 240),
    surfaceVariant = Color(30, 42, 58),
    onSurfaceVariant = Color(180, 192, 208),
    surfaceTint = Color(132, 158, 190),
    inverseSurface = Color(226, 232, 240),
    inverseOnSurface = Color(20, 30, 45),
    outline = Color(110, 124, 142),
    outlineVariant = Color(40, 54, 72),
    surfaceBright = Color(24, 42, 66),
    surfaceDim = Color(0, 6, 15),
    surfaceContainerLowest = Color(0, 6, 15),
    surfaceContainerLow = Color(0, 10, 24),
    surfaceContainer = Color(0, 13, 30),
    surfaceContainerHigh = Color(8, 24, 44),
    surfaceContainerHighest = Color(16, 33, 56),
)

val LightColorScheme = lightColorScheme(
    primary = Color(40, 40, 40),
    onPrimary = Color.White,
    primaryContainer = Color(244, 240, 231),
    onPrimaryContainer = Color(28, 27, 25),
    inversePrimary = Color(200, 198, 194),
    secondary = Color(143, 141, 141),
    onSecondary = Color.White,
    secondaryContainer = Color(232, 226, 214),
    onSecondaryContainer = Color(40, 38, 34),
    tertiary = Color(124, 110, 110),
    onTertiary = Color.White,
    tertiaryContainer = Color(238, 226, 222),
    onTertiaryContainer = Color(45, 35, 35),
    background = Color(254, 252, 242),
    onBackground = Color(28, 27, 24),
    surface = Color(246, 242, 233),
    onSurface = Color(28, 27, 24),
    surfaceVariant = Color(232, 226, 214),
    onSurfaceVariant = Color(78, 74, 66),
    surfaceTint = Color(40, 40, 40),
    inverseSurface = Color(49, 48, 44),
    inverseOnSurface = Color(245, 241, 232),
    outline = Color(128, 122, 112),
    outlineVariant = Color(208, 202, 190),
    surfaceBright = Color(254, 252, 242),
    surfaceDim = Color(222, 218, 208),
    surfaceContainerLowest = Color(255, 255, 250),
    surfaceContainerLow = Color(250, 247, 238),
    surfaceContainer = Color(244, 240, 231),
    surfaceContainerHigh = Color(238, 234, 224),
    surfaceContainerHighest = Color(232, 228, 218),
)

/**
 * Builds a complete scheme from one seed color, following Material's tone mapping
 * (e.g. primary = tone 40 in light / 80 in dark). Tones are approximated with HSL lightness.
 */
fun seedColorScheme(seed: Color, darkTheme: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    val hue = hsl[0]
    val chroma = hsl[1].coerceAtLeast(0.35f)

    fun palette(h: Float, s: Float): (Int) -> Color = { tone ->
        Color(ColorUtils.HSLToColor(floatArrayOf((h + 360f) % 360f, s, tone / 100f)))
    }

    val p = palette(hue, chroma)
    val s = palette(hue, chroma * 0.35f)
    val t = palette(hue + 60f, chroma * 0.5f)
    val n = palette(hue, 0.06f)
    val nv = palette(hue, 0.12f)

    return if (darkTheme) {
        darkColorScheme(
            primary = p(80), onPrimary = p(20),
            primaryContainer = p(30), onPrimaryContainer = p(90),
            inversePrimary = p(40),
            secondary = s(80), onSecondary = s(20),
            secondaryContainer = s(30), onSecondaryContainer = s(90),
            tertiary = t(80), onTertiary = t(20),
            tertiaryContainer = t(30), onTertiaryContainer = t(90),
            background = n(6), onBackground = n(90),
            surface = n(6), onSurface = n(90),
            surfaceVariant = nv(30), onSurfaceVariant = nv(80),
            surfaceTint = p(80),
            inverseSurface = n(90), inverseOnSurface = n(20),
            outline = nv(60), outlineVariant = nv(30),
            surfaceBright = n(24), surfaceDim = n(6),
            surfaceContainerLowest = n(4), surfaceContainerLow = n(10),
            surfaceContainer = n(12), surfaceContainerHigh = n(17),
            surfaceContainerHighest = n(22),
        )
    } else {
        lightColorScheme(
            primary = p(40), onPrimary = p(100),
            primaryContainer = p(90), onPrimaryContainer = p(10),
            inversePrimary = p(80),
            secondary = s(40), onSecondary = s(100),
            secondaryContainer = s(90), onSecondaryContainer = s(10),
            tertiary = t(40), onTertiary = t(100),
            tertiaryContainer = t(90), onTertiaryContainer = t(10),
            background = n(98), onBackground = n(10),
            surface = n(98), onSurface = n(10),
            surfaceVariant = nv(90), onSurfaceVariant = nv(30),
            surfaceTint = p(40),
            inverseSurface = n(20), inverseOnSurface = n(95),
            outline = nv(50), outlineVariant = nv(80),
            surfaceBright = n(98), surfaceDim = n(87),
            surfaceContainerLowest = n(100), surfaceContainerLow = n(96),
            surfaceContainer = n(94), surfaceContainerHigh = n(92),
            surfaceContainerHighest = n(90),
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkThemeSetting: SettingDarkMode = SettingDarkMode.Auto,
    themeSource: AppThemeSource = AppThemeSource.Device,
    dynamicColor: Boolean = true,
    imageBitmap: ImageBitmap? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkThemeSetting) {
        SettingDarkMode.Auto -> isSystemInDarkTheme()
        SettingDarkMode.On -> true
        SettingDarkMode.Off -> false
    }

    val context = LocalContext.current

    val palette = remember(imageBitmap, themeSource) {
        if (themeSource == AppThemeSource.Image) {
            imageBitmap?.asAndroidBitmap()?.let {
                Palette.from(it).generate()
            }
        } else null
    }

    val colorScheme = when {
        palette != null -> {
            // Replicate Pixel's "Seed Color" logic by picking the most representative color
            val seedColor =
                Color(palette.getVibrantColor(palette.getDominantColor(Color.Gray.toArgb())))
            seedColorScheme(seedColor, darkTheme)
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
