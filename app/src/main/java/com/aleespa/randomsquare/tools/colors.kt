package com.aleespa.randomsquare.tools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs

fun Color.contrasted(): Color {
    // Compute perceived luminance (0 = dark, 1 = light)
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue

    // If the color is dark, return white; otherwise, return black
    return if (luminance < 0.5) Color.White else Color.Black
}

fun colorToHexWithoutAlpha(color: Color): String {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    return String.format("#%02X%02X%02X", red, green, blue)
}

fun fromColor(color: Color): Int {
    return color.toArgb() // Convert Color to Int (ARGB format)
}


fun isColorDark(colorInt: Int): Boolean {
    val color = Color(colorInt)
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()

    // Perceived luminance formula
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b)

    return luminance < 128
}

fun intColorToHexWithoutAlpha(colorInt: Int): String {
    // Mask out the alpha channel and format as #RRGGBB
    val rgb = colorInt and 0x00FFFFFF
    return String.format("#%06X", rgb)
}


fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    val l = (max + min) / 2f
    val s: Float

    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = delta / (1f - kotlin.math.abs(2f * l - 1f))
        h = when (max) {
            r -> ((g - b) / delta) % 6f
            g -> (b - r) / delta + 2f
            else -> (r - g) / delta + 4f
        }
        h *= 60f
        if (h < 0f) h += 360f
    }
    return floatArrayOf(h, s, l)
}

fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}