package com.aleespa.randomsquare.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.pages.ColormapSelectionDialog
import com.aleespa.randomsquare.pages.visualize.BackgroundColorSelector
import com.aleespa.randomsquare.pages.visualize.ColormapDropdown
import com.aleespa.randomsquare.pages.visualize.TitleText
import com.aleespa.randomsquare.ui.theme.MyApplicationTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigurationWizard(onConfigFinished = { selectedFigures, bgColor, colormap ->
                        handleConfigurationFinished(selectedFigures, bgColor, colormap)
                    })
                }
            }
        }
    }

    private fun handleConfigurationFinished(
        selectedFigures: List<Figures>,
        bgColor: Int,
        colormap: Colormaps
    ) {
        val context = this
        MainScope().launch {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[SELECTED_FIGURES_KEY] = selectedFigures.joinToString(",") { it.key }
                    this[BG_COLOR_KEY] = bgColor
                    this[COLORMAP_KEY] = colormap.key
                }
            }
            WallpaperWidget().update(context, glanceId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    companion object {
        val SELECTED_FIGURES_KEY = stringPreferencesKey("selected_figures")
        val BG_COLOR_KEY = intPreferencesKey("bg_color")
        val COLORMAP_KEY = stringPreferencesKey("colormap")
        val SELECTED_FIGURE_KEY = stringPreferencesKey("selected_figure")
    }
}

@Composable
fun ConfigurationWizard(onConfigFinished: (List<Figures>, Int, Colormaps) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var selectedFigures by remember { mutableStateOf(setOf<Figures>()) }
    var selectedBgColor by remember { mutableIntStateOf(Color.Black.toArgb()) }
    var selectedColormap by remember { mutableStateOf(Colormaps.VIRIDIS) }
    var showColormapDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {

        if (showColormapDialog) {
            ColormapSelectionDialog(
                selectedColormap = selectedColormap,
                isFractal = false,
                onColormapChange = { selectedColormap = it },
                onDismiss = { showColormapDialog = false }
            )
        }

        if (selectedFigures.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                WidgetPreview(figure = selectedFigures.first())
            }
        }

        when (step) {
            1 -> {
                TitleText("Select Compositions")
                Spacer(Modifier.height(16.dp))
                val compositionFigures = Figures.entries.filter { it.figureType == FigureType.COMPOSITIONS }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(compositionFigures) { figure ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFigures = if (selectedFigures.contains(figure)) {
                                        selectedFigures - figure
                                    } else {
                                        selectedFigures + figure
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Checkbox(
                                checked = selectedFigures.contains(figure),
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(figure.resourceStringId))
                        }
                        HorizontalDivider()
                    }
                }
                Button(
                    onClick = {
                        step = 2
                    },
                    enabled = selectedFigures.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Continue")
                }
            }

            2 -> {
                TitleText("Customize Appearance")

                Column(modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(24.dp))
                    Text("Colormap", style = MaterialTheme.typography.titleMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ColormapDropdown(
                            selectedColormap = selectedColormap,
                            isFractal = false,
                            onShowDialogChange = { showColormapDialog = it }
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Background Color", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    BackgroundColorSelector(
                        bgColor = selectedBgColor,
                        onColorChange = { selectedBgColor = it }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfigFinished(
                                selectedFigures.toList(),
                                selectedBgColor,
                                selectedColormap
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Finish")
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPreview(figure: Figures) {
    val imageRes = figure.sampleImage
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Widget Preview",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    2.dp, MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
