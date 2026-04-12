package com.aleespa.randomsquare.widget

import android.app.WallpaperManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.getAppWidgetState
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.tools.convertToAspectRatio
import com.aleespa.randomsquare.tools.generate32BitSeed
import com.aleespa.randomsquare.tools.generateRandomPlot
import com.aleespa.randomsquare.tools.getScreenResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SampleGlanceWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = getAppWidgetState(
            context,
            androidx.glance.state.PreferencesGlanceStateDefinition,
            glanceId
        )

        // 1. Get selected figures and pick one at random
        val figureKeysString =
            prefs[WidgetConfigurationActivity.SELECTED_FIGURES_KEY] ?: Figures.SUPER_RANDOM.key
        val figureKeys = figureKeysString.split(",")
        val randomFigureKey = figureKeys.random()
        val figure = Figures.fromKey(randomFigureKey)

        // 2. Get background color
        val bgColor = prefs[WidgetConfigurationActivity.BG_COLOR_KEY] ?: Color.Black.toArgb()

        // 3. Get colormap
        val colormapKey = prefs[WidgetConfigurationActivity.COLORMAP_KEY]
        val colormap = Colormaps.fromKey(colormapKey, figure.figureType == FigureType.FRACTAL)

        withContext(Dispatchers.Default) {
            val seed = generate32BitSeed()
            val imageBitmap = generateRandomPlot(seed, bgColor, figure, colormap)
            val androidBitmap = imageBitmap?.asAndroidBitmap()

            if (androidBitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)

                val finalBitmap = if (figure.figureType != FigureType.COMPOSITIONS) {
                    val resolution = getScreenResolution(context)
                    convertToAspectRatio(
                        androidBitmap,
                        resolution[0],
                        resolution[1],
                        Color(bgColor)
                    )
                } else {
                    androidBitmap
                }

                try {
                    wallpaperManager.setBitmap(finalBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
