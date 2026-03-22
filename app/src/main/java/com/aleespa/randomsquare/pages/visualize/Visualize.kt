package com.aleespa.randomsquare.pages.visualize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.AspectRatioDialog
import com.aleespa.randomsquare.tools.loadBitmapFromFile


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Visualize(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    BackHandler {
        navController.navigate(BottomBarScreen.Browse.route)
    }

    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if ((savedBitmap != null).and(visualizeModel.isFromGallery.not())) {
        visualizeModel.imageBitmapState = savedBitmap?.asImageBitmap()
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
        item { SeedText(visualizeModel)}
        item { Spacer(Modifier.height(2.dp)) }
        if (visualizeModel.selectedFigure.figureType == FigureType.COMPOSITIONS) {
            item { Spacer(Modifier.height(35.dp)) }
        }
        item { GeneratePlotButton(visualizeModel, context) }
        if (visualizeModel.selectedFigure.figureType != FigureType.COMPOSITIONS) {
            item { Spacer(Modifier.height(30.dp)) }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ColormapDropdown(
                        visualizeModel = visualizeModel
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { BackgroundColorSelector(visualizeModel) }

        }
        else{
            item { Spacer(Modifier.height(25.dp)) }
            item { SeedButton(visualizeModel)}
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
