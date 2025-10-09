package com.aleespa.randomsquare.widget

import android.content.Context
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize

class SampleGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = androidx.glance.layout.Alignment.Center
            ) {
                Button(
                    text = "Click me",
                    onClick = actionRunCallback<SampleGlanceWidgetActionCallback>()
                )
            }
        }
    }
}
