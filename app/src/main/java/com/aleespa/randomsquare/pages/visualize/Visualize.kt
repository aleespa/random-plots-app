package com.aleespa.randomsquare.pages.visualize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.AspectRatioDialog
import com.aleespa.randomsquare.tools.LatexMathView
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
        item { Spacer(Modifier.height(2.dp)) }

        with(visualizeModel.selectedFigure) {
            menuType(visualizeModel)
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
