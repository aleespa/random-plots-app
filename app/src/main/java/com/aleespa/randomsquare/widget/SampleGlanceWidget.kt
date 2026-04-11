package com.aleespa.randomsquare.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.widget.WidgetConfigurationActivity.Companion.SELECTED_FIGURES_KEY

class SampleGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            // Use SELECTED_FIGURES_KEY and pick the first one for the preview
            val figureKeysString = prefs[SELECTED_FIGURES_KEY] ?: Figures.SUPER_RANDOM.key
            val firstFigureKey = figureKeysString.split(",").first()
            val figure = Figures.fromKey(firstFigureKey)
            val imageRes = figure.sampleImage

            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(imageRes),
                    contentDescription = "Generate Plot",
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionRunCallback<SampleGlanceWidgetActionCallback>()),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
