package com.aleespa.randomsquare.pages.visualize

import android.app.Activity
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ExperimentalMaterial3ExpressiveApi
fun GeneratePlotButton(
    visualizeModel: VisualizeModel,
    context: Context
) {
    var pressedIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    ButtonGroup(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 300.dp)
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState)
        },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        content = {
            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 0) 1.1f else 1f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 0) 8.dp else 24.dp,
                        label = "corner"
                    )
                    val innerCornerRadius by animateDpAsState(
                        if (pressedIndex == 0) 12.dp else 8.dp,
                        label = "innerCorner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 0
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 0) pressedIndex = -1
                            }
                            val figureType = visualizeModel.selectedFigure.figureType
                            if (figureType == FigureType.FRACTAL ||
                                figureType == FigureType.COMPOSITIONS
                            ) {
                                visualizeModel.toFitAspectRatio = false
                                setWallpaperAfterAd(visualizeModel, context)
                            } else {
                                visualizeModel.showAspectRatioDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = cornerRadius,
                            bottomStart = cornerRadius,
                            topEnd = innerCornerRadius,
                            bottomEnd = innerCornerRadius
                        ),
                        modifier = Modifier
                            .weight(weight)
                            .height(56.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Set Wallpaper",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                menuContent = {
                    Text(text = "Set Wallpaper")
                }
            )

            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 1) 3.4f else 3f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 1) 12.dp else 8.dp,
                        label = "corner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 1
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 1) pressedIndex = -1
                            }
                            val shouldShowAd = (generate32BitSeed() % AD_FREQUENCY).toInt() == 0
                            val activity = context as? Activity

                            if (shouldShowAd && activity != null) {
                                AdManager.showIfReady(activity) {
                                    generateNewPlot(visualizeModel, context)
                                }
                            } else {
                                generateNewPlot(visualizeModel, context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(cornerRadius),
                        modifier = Modifier
                            .weight(weight)
                            .height(56.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Casino,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = context.getString(R.string.generate),
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                menuContent = {
                    Text(text = context.getString(R.string.generate))
                }
            )

            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 2) 1.1f else 1f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 2) 8.dp else 24.dp,
                        label = "corner"
                    )
                    val innerCornerRadius by animateDpAsState(
                        if (pressedIndex == 2) 12.dp else 8.dp,
                        label = "innerCorner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 2
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 2) pressedIndex = -1
                            }
                            if (visualizeModel.isFromGallery.not()) {
                                saveToGallery(visualizeModel, context)
                            } else {
                                deleteFromGallery(visualizeModel, context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = innerCornerRadius,
                            bottomStart = innerCornerRadius,
                            topEnd = cornerRadius,
                            bottomEnd = cornerRadius
                        ),
                        modifier = Modifier
                            .weight(weight)
                            .height(56.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        if (visualizeModel.isSavingLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (visualizeModel.isFromGallery.not()) Icons.Default.StarBorder else Icons.Default.Star,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                },
                menuContent = {
                    Text(text = if (visualizeModel.isFromGallery.not()) "Save to Gallery" else "Delete from Gallery")
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@ExperimentalMaterial3ExpressiveApi
fun FractalActions(
    visualizeModel: VisualizeModel,
    context: Context
) {
    var pressedIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    ButtonGroup(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 260.dp)
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState)
        },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        content = {
            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 0) 1.1f else 1f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 0) 8.dp else 24.dp,
                        label = "corner"
                    )
                    val innerCornerRadius by animateDpAsState(
                        if (pressedIndex == 0) 12.dp else 8.dp,
                        label = "innerCorner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 0
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 0) pressedIndex = -1
                            }
                            visualizeModel.resetFractalSettings()
                            generateNewPlot(
                                visualizeModel,
                                context,
                                randomizeSeed = false,
                                showAds = false
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = cornerRadius,
                            bottomStart = cornerRadius,
                            topEnd = innerCornerRadius,
                            bottomEnd = innerCornerRadius
                        ),
                        modifier = Modifier
                            .weight(weight)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LayersClear,
                            contentDescription = "Reset",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                menuContent = {
                    Text(text = "Reset")
                }
            )

            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 1) 1.1f else 1f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 1) 12.dp else 8.dp,
                        label = "corner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 1
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 1) pressedIndex = -1
                            }
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(cornerRadius),
                        modifier = Modifier
                            .weight(weight)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Set Wallpaper",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                menuContent = {
                    Text(text = "Set Wallpaper")
                }
            )

            customItem(
                buttonGroupContent = {
                    val weight by animateFloatAsState(
                        if (pressedIndex == 2) 1.1f else 1f,
                        label = "weight"
                    )
                    val cornerRadius by animateDpAsState(
                        if (pressedIndex == 2) 8.dp else 24.dp,
                        label = "corner"
                    )
                    val innerCornerRadius by animateDpAsState(
                        if (pressedIndex == 2) 12.dp else 8.dp,
                        label = "innerCorner"
                    )
                    Button(
                        onClick = {
                            pressedIndex = 2
                            scope.launch {
                                delay(300)
                                if (pressedIndex == 2) pressedIndex = -1
                            }
                            if (visualizeModel.isFromGallery.not()) {
                                saveToGallery(visualizeModel, context)
                            } else {
                                deleteFromGallery(visualizeModel, context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = innerCornerRadius,
                            bottomStart = innerCornerRadius,
                            topEnd = cornerRadius,
                            bottomEnd = cornerRadius
                        ),
                        modifier = Modifier
                            .weight(weight)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        if (visualizeModel.isSavingLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (visualizeModel.isFromGallery.not()) Icons.Default.StarBorder else Icons.Default.Star,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                },
                menuContent = {
                    Text(text = if (visualizeModel.isFromGallery.not()) "Save to Gallery" else "Delete from Gallery")
                }
            )
        }
    )
}
