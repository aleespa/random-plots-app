package com.aleespa.randomsquare.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.min
import kotlin.math.max

enum class SettingDarkMode(var text: String) {
    Auto("Auto"),
    On("Dark"),
    Off("Light")
}

enum class BackgroundColors(var color: Color, var type: String) {
    // Dark Colors
    BLACK(Color(0, 0, 0), "Dark"),
    RED_DARK(Color(32, 15, 0), "Dark"),
    GREEN_DARK(Color(0, 30, 13), "Dark"),
    BLUE_DARK(Color(0, 13, 30), "Dark"),
    PURPLE_DARK(Color(22, 0, 31), "Dark"),
    BROWN_DARK(Color(30, 15, 10), "Dark"),
    GRAY_DARK(Color(22, 22, 22), "Dark"),

    // Light Colors
    WHITE(Color(255, 255, 255), "Light"),
    SAND(Color(244, 240, 231), "Light"),
    GREEN_LIGHT(Color(234, 250, 241), "Light"),
    PINK_LIGHT(Color(251, 238, 230), "Light"),
    BLUE_LIGHT(Color(230, 240, 250), "Light"),
    LAVENDER_LIGHT(Color(245, 230, 250), "Light"),
    YELLOW_LIGHT(Color(255, 253, 231), "Light"),
    PEACH_LIGHT(Color(255, 239, 213), "Light")
}

fun getBackgroundColorsByType(type: String): List<BackgroundColors> {
    return BackgroundColors.entries.filter { it.type == type }

}

fun getBackgroundColorByColor(color: Color): BackgroundColors {
    return BackgroundColors.entries.find { it.color == color } ?: BackgroundColors.BLACK
}

fun Color.adjustColor(brightnessFactor: Float = 0f, saturationFactor: Float = 0f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)

    // Adjust brightness
    if (brightnessFactor > 0) {
        hsv[2] = min(hsv[2] + brightnessFactor, 1f) // Increase brightness
    } else if (brightnessFactor < 0) {
        hsv[2] = max(hsv[2] + brightnessFactor, 0f) // Decrease brightness
    }

    hsv[1] = min(max(hsv[1] + saturationFactor, 0f), 1f) // Ensure within bounds

    return Color(android.graphics.Color.HSVToColor(hsv))
}
