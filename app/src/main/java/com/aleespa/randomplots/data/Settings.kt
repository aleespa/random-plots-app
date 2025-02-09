package com.aleespa.randomplots.data

import androidx.compose.ui.graphics.Color

enum class SettingDarkMode(var text: String) {
    Auto("Auto"),
    On("Dark"),
    Off("Light")
}

enum class BackgroundColors(var color:Color, var type: String) {
    BLACK(Color(0, 0, 0), "Dark"),
    RED_DARK(Color(30, 13, 0), "Dark"),
    GREEN_DARK(Color(0, 30, 13), "Dark"),
    BLUE_DARK(Color(0, 13, 30), "Dark"),
    WHITE(Color(255, 255, 255), "Light"),
    SAND(Color(244, 240, 231), "Light"),
    GREEN_LIGHT(Color(234, 250, 241), "Light"),
    PINK_LIGHT(Color(251, 238, 230), "Light")
}

fun getBackgroundColorsByType(type: String): List<BackgroundColors> {
    return BackgroundColors.entries.filter { it.type == type }
}