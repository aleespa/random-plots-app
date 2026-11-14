package com.aleespa.randomsquare.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.aleespa.randomsquare.MainActivity
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.tools.QuickArt
import com.aleespa.randomsquare.tools.toJson
import java.io.File

/**
 * Home-screen widget showing a random figure. Tapping it renders a new one on the device; the
 * corner button opens that exact figure in the app. Each widget keeps its image in
 * `files/widget/<appWidgetId>-<time>.png` and the figure's settings (JSON) in its Glance state.
 */
class ArtWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        if (state[FileKey]?.let { File(it).exists() } != true) {
            newArt(context, id)
            state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        }
        provideContent {
            // The session read its state before the first figure above was stored, so fall back
            // to the values just written; later taps update the session state itself.
            val prefs = currentState<Preferences>()
            val path = prefs[FileKey] ?: state[FileKey]
            val bitmap = remember(path) { path?.let { BitmapFactory.decodeFile(it) } }
            GlanceTheme { Content(bitmap, prefs[RecipeKey] ?: state[RecipeKey]) }
        }
    }

    @Composable
    private fun Content(bitmap: Bitmap?, recipe: String?) {
        val context = LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(24.dp)
                .background(GlanceTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = context.getString(R.string.art_widget_new),
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionRunCallback<NewArtAction>())
                )
            } else {
                CircularProgressIndicator(color = GlanceTheme.colors.primary)
            }
            if (recipe != null) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_open_in_app),
                        contentDescription = context.getString(R.string.art_widget_open),
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                        modifier = GlanceModifier
                            .size(32.dp)
                            .cornerRadius(16.dp)
                            .background(ColorProvider(Color(0x66000000)))
                            .padding(6.dp)
                            .clickable(
                                actionStartActivity(
                                    Intent(context, MainActivity::class.java)
                                        .setAction(MainActivity.ACTION_OPEN_RECIPE)
                                        .putExtra(MainActivity.EXTRA_RECIPE, recipe)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                )
                            )
                    )
                }
            }
        }
    }

    companion object {
        val FileKey = stringPreferencesKey("art_file")
        val RecipeKey = stringPreferencesKey("art_recipe")

        /** Square size of the widget image; RemoteViews bitmaps must stay small. */
        private const val SIZE = 600

        private fun dir(context: Context) = File(context.filesDir, "widget")

        /** Renders a new random figure for widget [id] and stores it in the widget's state. */
        suspend fun newArt(context: Context, id: GlanceId) {
            val (entity, bitmap) = QuickArt.render(SIZE, SIZE) ?: return
            val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
            val file = File(dir(context).apply { mkdirs() }, "$widgetId-${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[FileKey]?.let { old -> if (old != file.path) File(old).delete() }
                    this[FileKey] = file.path
                    this[RecipeKey] = entity.toJson().toString()
                }
            }
        }

        /** Deletes the images of removed widgets. */
        fun deleteFiles(context: Context, widgetIds: IntArray) {
            val prefixes = widgetIds.map { "$it-" }
            dir(context).listFiles()?.forEach { f ->
                if (prefixes.any { f.name.startsWith(it) }) f.delete()
            }
        }
    }
}

class NewArtAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        ArtWidget.newArt(context, glanceId)
        ArtWidget().update(context, glanceId)
    }
}

class ArtWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArtWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        ArtWidget.deleteFiles(context, appWidgetIds)
    }
}
