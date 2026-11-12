package com.aleespa.randomsquare.pages.visualize

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.ui.components.AppTopBar


/**
 * Visualize screen header: the shared [AppTopBar] (insets are already handled by the screen),
 * with reload on the left and the overflow menu on the right.
 */
@Composable
fun HeaderSection(visualizeModel: VisualizeModel, context: Context) {
    var showMenu by remember { mutableStateOf(false) }
    AppTopBar(
        title = stringResource(visualizeModel.selectedFigure.resourceStringId),
        containerColor = Color.Transparent, // sits on the blurred image background
        windowInsets = WindowInsets(0),
        navigationIcon = {
            IconButton(onClick = {
                com.aleespa.randomsquare.tools.generateNewPlot(
                    visualizeModel,
                    context,
                    randomizeSeed = false,
                    showAds = false,
                    resetGalleryState = false
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.reload)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                VisualizeOverflowMenu(visualizeModel, context, showMenu) { showMenu = false }
            }
        }
    )
}


@Composable
private fun VisualizeOverflowMenu(
    visualizeModel: VisualizeModel,
    context: Context,
    showMenu: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onDismiss() }
    ) {
        DropdownMenuItem(
            text = {
                if (visualizeModel.isFromGallery.not())
                    Text(stringResource(id = R.string.save))
                else Text(stringResource(id = R.string.delete_from_gallery))
            },
            leadingIcon = {
                // Same icons as the save button: outline star = save, filled star = in gallery.
                if (visualizeModel.isFromGallery.not()) DropDownMenuIcon(Icons.Default.StarBorder)
                else DropDownMenuIcon(Icons.Default.Star)
            },
            onClick = {
                if (visualizeModel.isFromGallery.not()) {
                    saveToGallery(visualizeModel, context)
                } else {
                    deleteFromGallery(visualizeModel, context)
                }
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.set_wallpaper)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.AddPhotoAlternate) },
            onClick = {
                val figureType = visualizeModel.selectedFigure.figureType
                if (figureType == com.aleespa.randomsquare.FigureType.FRACTAL ||
                    figureType == com.aleespa.randomsquare.FigureType.COMPOSITIONS
                ) {
                    visualizeModel.toFitAspectRatio = false
                    com.aleespa.randomsquare.tools.generateNewPlot(
                        visualizeModel,
                        context,
                        randomizeSeed = false,
                        showAds = true,
                        width = 1500,
                        height = 1500,
                        resetGalleryState = false
                    ) {
                        setWallpaperAfterAd(visualizeModel, context)
                    }
                } else {
                    visualizeModel.showAspectRatioDialog = true
                }
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.share)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.Share) },
            onClick = {
                shareImageBitmap(visualizeModel, context)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.more_info)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.Info) },
            onClick = {
                visualizeModel.showInfo = !visualizeModel.showInfo
                onDismiss()
            }
        )
    }
}


/** Leading icon for dropdown menu items (standard 24.dp size, decorative). */
@Composable
fun DropDownMenuIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null
    )
}


fun shareImageBitmap(visualizeModel: VisualizeModel, context: Context) {
    val imageUri = visualizeModel.imageBitmapState?.let { saveImageBitmapToCache(it, context) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_text)))
}
