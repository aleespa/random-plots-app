package com.aleespa.randomsquare.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.aleespa.randomsquare.R
import kotlin.random.Random

class CoinFlipWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val ResultKey = stringPreferencesKey("coin_flip_result")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val result = prefs[ResultKey] ?: context.getString(R.string.heads)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(24.dp)
                .padding(8.dp)
                .clickable(actionRunCallback<CoinFlipActionCallback>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🪙",
                style = TextStyle(
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = result,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RandomWidget.Parkinsans,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class CoinFlipActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val result = if (Random.nextBoolean()) {
                context.getString(R.string.heads)
            } else {
                context.getString(R.string.tails)
            }
            prefs[CoinFlipWidget.ResultKey] = result
        }
        CoinFlipWidget().update(context, glanceId)
    }
}

class CoinFlipWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CoinFlipWidget()
}
