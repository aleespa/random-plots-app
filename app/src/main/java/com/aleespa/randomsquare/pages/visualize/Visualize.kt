package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.AspectRatioDialog
import com.aleespa.randomsquare.pages.ColormapSelectionDialog
import com.aleespa.randomsquare.tools.generateNewtonLatex
import com.aleespa.randomsquare.tools.loadBitmapFromFile
import com.aleespa.randomsquare.tools.readTexAssets

/** Single-column content never gets wider than this (keeps the square image sane on tablets). */
private val MaxSingleColumnWidth = 600.dp

/** Controls column width cap in the two-pane layout. */
private val MaxControlsPaneWidth = 420.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Visualize(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    // Use LaunchedEffect to load cache only once when needed,
    // and only if we are NOT viewing an image from the gallery.
    LaunchedEffect(visualizeModel.isFromGallery, visualizeModel.selectedFigure) {
        if (!visualizeModel.isFromGallery && visualizeModel.imageBitmapState == null) {
            val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
            if (savedBitmap != null) {
                visualizeModel.imageBitmapState = savedBitmap.asImageBitmap()
                if (visualizeModel.latexString.isEmpty()) {
                    visualizeModel.latexString =
                        readTexAssets(context, visualizeModel.selectedFigure.key)
                }
                if (visualizeModel.selectedFigure == Figures.NEWTON && visualizeModel.newtonLatexString.isEmpty()) {
                    visualizeModel.newtonLatexString =
                        generateNewtonLatex(visualizeModel.newtonCoeffs)
                }
            }
        }
    }
    if (visualizeModel.showAspectRatioDialog) {
        AspectRatioDialog(
            visualizeModel,
            onDismiss = { visualizeModel.showAspectRatioDialog = false },
            onConfirm = {
                visualizeModel.showAspectRatioDialog = false
                setWallpaperAfterAd(visualizeModel, context)
            }
        )
    }

    if (visualizeModel.showColormapDialog) {
        val isFractal = visualizeModel.selectedFigure.figureType == FigureType.FRACTAL
        ColormapSelectionDialog(
            selectedColormap = visualizeModel.selectedColormap,
            isFractal = isFractal,
            onColormapChange = {
                visualizeModel.selectedColormap = it
                com.aleespa.randomsquare.tools.generateNewPlot(
                    visualizeModel,
                    context,
                    randomizeSeed = false,
                    showAds = false
                )
            },
            onDismiss = { visualizeModel.showColormapDialog = false }
        )
    }

    val isDark = when (visualizeModel.settingDarkMode) {
        SettingDarkMode.Auto -> isSystemInDarkTheme()
        SettingDarkMode.On -> true
        SettingDarkMode.Off -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        visualizeModel.imageBitmapState?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(
                                alpha = 0.6f
                            ),
                            blendMode = BlendMode.SrcOver
                        )
                    }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val useTwoPanes = maxWidth >= 600.dp && maxWidth > maxHeight
            if (useTwoPanes) {
                TwoPaneVisualize(visualizeModel)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxHeight()
                        .widthIn(max = MaxSingleColumnWidth),
                ) {
                    item { Spacer(Modifier.height(20.dp)) }
                    item { HeaderSection(visualizeModel, context) }
                    item { Spacer(Modifier.height(18.dp)) }
                    item { VisualizeBox(visualizeModel) }
                    item { SeedText(visualizeModel) }
                    item { Spacer(Modifier.height(6.dp)) }

                    with(visualizeModel.selectedFigure) {
                        menuType(visualizeModel)
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

/**
 * Landscape / wide layout: the image takes the full available height on the left, the header
 * and controls scroll in a column on the right.
 */
@Composable
private fun TwoPaneVisualize(visualizeModel: VisualizeModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VisualizeBox(
                    visualizeModel,
                    modifier = Modifier.aspectRatio(1f, matchHeightConstraintsFirst = true)
                )
            }
            SeedText(visualizeModel)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = MaxControlsPaneWidth)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item { HeaderSection(visualizeModel, context) }
            item { Spacer(Modifier.height(12.dp)) }

            with(visualizeModel.selectedFigure) {
                menuType(visualizeModel)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
