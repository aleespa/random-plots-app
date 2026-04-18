package com.aleespa.randomsquare.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.AppThemeSource
import androidx.compose.material3.ColorScheme

val DarkColorScheme = darkColorScheme(
    primary = Color(132, 158, 190, 255),
    secondary = Color(91, 120, 157, 255),
    tertiary = Color(38, 61, 91, 255),
    background = Color(0, 6, 15),
    surface = Color(0, 13, 30),
    primaryContainer = Color(0, 13, 30),
    surfaceContainer = Color(0, 13, 30),
)

val LightColorScheme = lightColorScheme(
    primary = Color(40, 40, 40, 255),
    secondary = Color(143, 141, 141, 255),
    tertiary = Color(124, 110, 110, 255),
    background = Color(254, 252, 242),
    surface = Color(246, 242, 233),
    primaryContainer = Color(244, 240, 231),
    surfaceContainer = Color(244, 240, 231),
)

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
            val seedColor = Color(palette.getVibrantColor(palette.getDominantColor(Color.Gray.toArgb())))
            
            if (darkTheme) {
                darkColorScheme(
                    primary = lerp(seedColor, Color.White, 0.4f), // T80
                    onPrimary = lerp(seedColor, Color.Black, 0.8f),
                    primaryContainer = lerp(seedColor, Color.Black, 0.6f), // T30
                    onPrimaryContainer = lerp(seedColor, Color.White, 0.8f),
                    secondary = lerp(seedColor, Color.White, 0.2f),
                    onSecondary = Color.Black,
                    background = lerp(seedColor, Color.Black, 0.95f), // Very dark background
                    surface = lerp(seedColor, Color.Black, 0.9f),
                    onSurface = Color.White
                )
            } else {
                lightColorScheme(
                    primary = lerp(seedColor, Color.Black, 0.4f), // T40
                    onPrimary = Color.White,
                    primaryContainer = lerp(seedColor, Color.White, 0.8f), // T90
                    onPrimaryContainer = lerp(seedColor, Color.Black, 0.7f),
                    secondary = lerp(seedColor, Color.Black, 0.2f),
                    onSecondary = Color.White,
                    background = lerp(seedColor, Color.White, 0.95f), // Very light background
                    surface = lerp(seedColor, Color.White, 0.9f),
                    onSurface = Color.Black
                )
            }
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