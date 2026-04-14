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
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

class RandomWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val Parkinsans = FontFamily("parkinsans")
        val NumbersKey = stringPreferencesKey("random_numbers")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val defaultNumbers = List(48) {
            (1..4).map { _ -> charPool.random() }.joinToString("")
        }.joinToString(",")
        val numbersString = prefs[NumbersKey] ?: defaultNumbers
        val numbers = numbersString.split(",")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(24.dp)
                .padding(8.dp)
                .clickable(actionRunCallback<RandomWidgetActionCallback>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Grid content
            for (i in 0 until 8) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (j in 0 until 6) {
                        val index = i * 6 + j
                        if (index < numbers.size) {
                            Box(
                                modifier = GlanceModifier.defaultWeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = numbers[index],
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = Parkinsans,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
