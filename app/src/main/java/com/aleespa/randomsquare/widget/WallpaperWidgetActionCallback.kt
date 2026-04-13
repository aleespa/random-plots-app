package com.aleespa.randomsquare.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlin.random.Random

class WallpaperWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        
        // Generate new 4-character alphanumeric codes and store them in GlanceState (6x7 = 42)
        updateAppWidgetState(context, glanceId) { prefs ->
            val newNumbers = List(42) {
                (1..4).map { _ -> charPool.random() }.joinToString("")
            }.joinToString(",")
            prefs[WallpaperWidget.NumbersKey] = newNumbers
        }
        
        // Notify the widget to update its content
        WallpaperWidget().update(context, glanceId)
    }
}
