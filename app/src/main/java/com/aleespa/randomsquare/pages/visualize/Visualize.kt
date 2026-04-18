package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                    visualizeModel.latexString = readTexAssets(context, visualizeModel.selectedFigure.key)
                }
                if (visualizeModel.selectedFigure == Figures.NEWTON && visualizeModel.newtonLatexString.isEmpty()) {
                    visualizeModel.newtonLatexString = generateNewtonLatex(visualizeModel.newtonCoeffs)
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
                            color = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
                            blendMode = BlendMode.SrcOver
                        )
                    }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
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

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
