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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.aleespa.randomsquare.data.SettingDarkMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(132, 158, 190, 255),
    secondary = Color(91, 120, 157, 255),
    tertiary = Color(38, 61, 91, 255),
    background = Color(0, 6, 15),
    surface = Color(0, 13, 30),
    primaryContainer = Color(0, 13, 30),
    surfaceContainer = Color(0, 13, 30),
)

private val LightColorScheme = lightColorScheme(
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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkThemeSetting) {
        SettingDarkMode.Auto -> isSystemInDarkTheme()
        SettingDarkMode.On -> true
        SettingDarkMode.Off -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
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