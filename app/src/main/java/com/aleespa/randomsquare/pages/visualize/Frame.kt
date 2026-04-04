package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.LatexMathView
import com.aleespa.randomsquare.tools.contrasted
import com.aleespa.randomsquare.tools.generateNewPlot


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VisualizeBox(visualizeModel: VisualizeModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .aspectRatio(1f)
            .clickable { visualizeModel.showInfo = !visualizeModel.showInfo }
            .then(
                if (visualizeModel.selectedFigure.figureType == FigureType.FRACTAL) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                offset += pan
                                scale *= zoom
                            }
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val allPointersUp = event.changes.all { !it.pressed }
                                    if (allPointersUp && (offset != Offset.Zero || scale != 1f)) {
                                        val currentZoom = visualizeModel.fractalZoom
                                        val finalOffset = offset
                                        val finalScale = scale

                                        // Delta in normalized device coordinates (-1 to 1)
                                        val deltaX = (finalOffset.x / size.width.toDouble()) * 2.0 * currentZoom
                                        val deltaY = (finalOffset.y / size.height.toDouble()) * 2.0 * currentZoom

                                        // Apply translation first (relative to current zoom)
                                        visualizeModel.fractalXCenter -= deltaX / finalScale
                                        visualizeModel.fractalYCenter += deltaY / finalScale

                                        // Then update zoom
                                        visualizeModel.fractalZoom /= finalScale

                                        generateNewPlot(
                                            visualizeModel,
                                            context,
                                            randomizeSeed = false,
                                            showAds = false
                                        ) {
                                            offset = Offset.Zero
                                            scale = 1f
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    Modifier
                }
            ),
    ) {
        if (!visualizeModel.showInfo) {
            val isFractal = visualizeModel.selectedFigure.figureType == FigureType.FRACTAL
            if (visualizeModel.loadingPlotGenerator && !isFractal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator(
                        containerColor = Color(visualizeModel.bgColor).contrasted(),
                        indicatorColor = Color(visualizeModel.bgColor)
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    ImageWithNullFallback(
                        imageBitmap = visualizeModel.imageBitmapState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(visualizeModel.bgColor)),
                contentAlignment = Alignment.Center
            ) {
                LatexMathView(visualizeModel)
            }
        }
    }
}

@Composable
fun ImageWithNullFallback(imageBitmap: ImageBitmap?, modifier: Modifier = Modifier) {
    val painter = if (imageBitmap != null) {
        BitmapPainter(imageBitmap)
    } else {
        painterResource(id = R.drawable.cover_random)
    }
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SeedText(visualizeModel: VisualizeModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (visualizeModel.selectedFigure.figureType == FigureType.FRACTAL) {
            Text(
                text = "x: ${String.format("%.4f", visualizeModel.fractalXCenter)}  " +
                        "y: ${String.format("%.4f", visualizeModel.fractalYCenter)}  " +
                        "zoom: ${String.format("%.2e", 1.0 / visualizeModel.fractalZoom)}" + "x",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Text(
                text = "Seed: ${visualizeModel.randomSeed}",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
