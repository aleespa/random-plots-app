package com.aleespa.randomsquare.tools

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel


data class PlotSettings(
    val color: Int,
    val shape: String,
    val size: Float
)

class SettingsViewModel : ViewModel() {
    fun setSettings(plotSettings: PlotSettings) {
        this.plotSettings = plotSettings.copy()
    }
    var plotSettings by mutableStateOf(
        PlotSettings(Color.Blue.value.toInt(),
            "circle",
            10f))
}
