package com.aleespa.randomsquare.tools

import androidx.compose.ui.graphics.Color

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