package com.aleespa.randomsquare.data

import androidx.compose.ui.graphics.Color

enum class SettingDarkMode(var text: String) {
    Auto("Auto"),
    On("Dark"),
    Off("Light")
}

enum class BackgroundColors(var color: Color, var type: String) {
    // Dark Colors
    BLACK(Color(0, 0, 0), "Dark"),
    RED_DARK(Color(30, 13, 0), "Dark"),
    GREEN_DARK(Color(0, 30, 13), "Dark"),
    BLUE_DARK(Color(0, 13, 30), "Dark"),
    PURPLE_DARK(Color(20, 0, 30), "Dark"),
    BROWN_DARK(Color(30, 15, 10), "Dark"),
    GRAY_DARK(Color(20, 20, 20), "Dark"),

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
