package com.aleespa.randomsquare.pages.visualize

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.AD_FREQUENCY
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.ads.AdManager
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.generate32BitSeed
import com.aleespa.randomsquare.tools.generateNewPlot


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ExperimentalMaterial3ExpressiveApi
fun GeneratePlotButton(
    visualizeModel: VisualizeModel,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(26.dp)
                .clickable {
                    val figureType = visualizeModel.selectedFigure.figureType
                    if (figureType == FigureType.FRACTAL ||
                        figureType == FigureType.COMPOSITIONS
                    ) {
                        visualizeModel.toFitAspectRatio = false
                        setWallpaperAfterAd(visualizeModel, context)
                    } else {
                        visualizeModel.showAspectRatioDialog = true
                    }
                }
        )
        ExtendedFloatingActionButton(
            elevation = FloatingActionButtonDefaults.elevation(10.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = {
                val shouldShowAd = (generate32BitSeed().toLong() % AD_FREQUENCY).toInt() == 0
                val activity = context as? Activity

                if (shouldShowAd && activity != null) {
                    // Show ad; generate the plot only after the ad is dismissed (or fails).
                    AdManager.showIfReady(activity) {
                        generateNewPlot(visualizeModel, context)
                    }
                } else {
                    generateNewPlot(visualizeModel, context)
                }
            },
            icon = {
                Icon(
                    Icons.Default.Casino,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            text = {
                Text(
                    text = context.getString(R.string.generate),
                    textAlign = TextAlign.Center,
                )
            }
        )

        if (visualizeModel.isSavingLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        } else {
            Icon(
                imageVector = if (visualizeModel.isFromGallery.not()) Icons.Default.StarBorder else Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        if (visualizeModel.isFromGallery.not()) {
                            saveToGallery(visualizeModel, context)
                        } else {
                            deleteFromGallery(visualizeModel, context)
                        }
                    }
            )
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
fun FractalActions(
    visualizeModel: VisualizeModel,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LayersClear,
            contentDescription = "Reset",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    visualizeModel.resetFractalSettings()
                    generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
                }
        )
        Spacer(Modifier.width(32.dp))
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    val figureType = visualizeModel.selectedFigure.figureType
                    if (figureType == FigureType.FRACTAL ||
                        figureType == FigureType.COMPOSITIONS
                    ) {
                        visualizeModel.toFitAspectRatio = false
                        generateNewPlot(
                            visualizeModel,
                            context,
                            randomizeSeed = false,
                            showAds = true,
                            resetGalleryState = false
                        ) {
                            setWallpaperAfterAd(visualizeModel, context)
                        }
                    } else {
                        visualizeModel.showAspectRatioDialog = true
                    }
                }
        )
        Spacer(Modifier.width(32.dp))
        if (visualizeModel.isSavingLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        } else {
            Icon(
                imageVector = if (visualizeModel.isFromGallery.not()) Icons.Default.StarBorder else Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (visualizeModel.isFromGallery.not()) {
                            saveToGallery(visualizeModel, context)
                        } else {
                            deleteFromGallery(visualizeModel, context)
                        }
                    }
            )
        }
    }
}
