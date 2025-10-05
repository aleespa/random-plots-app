package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.LatexMathView
import com.aleespa.randomsquare.tools.contrasted


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VisualizeBox(visualizeModel: VisualizeModel) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = Modifier
            .aspectRatio(1f)
            .padding(10.dp)
            .clickable { visualizeModel.showInfo = !visualizeModel.showInfo },
    ) {
        if (!visualizeModel.showInfo) {
            if (visualizeModel.loadingPlotGenerator) {
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
                ImageWithNullFallback(visualizeModel.imageBitmapState)
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
fun ImageWithNullFallback(imageBitmap: ImageBitmap?) {
    val painter = if (imageBitmap != null) {
        BitmapPainter(imageBitmap)
    } else {
        painterResource(id = R.drawable.cover_random)
    }
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}