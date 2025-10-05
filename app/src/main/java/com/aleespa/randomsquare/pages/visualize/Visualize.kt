package com.aleespa.randomsquare.pages.visualize

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.loadBitmapFromFile
import com.aleespa.randomsquare.tools.loadSavedImage
import com.aleespa.randomsquare.tools.saveBitmapToGallery
import com.aleespa.randomsquare.tools.setWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Visualize(
    visualizeModel: VisualizeModel,
    navController: NavHostController,
    showAd: () -> Unit
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
        _root_ide_package_.com.aleespa.randomsquare.pages.AspectRatioDialog(
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

        item { Spacer(Modifier.height(2.dp)) }
        if (visualizeModel.selectedFigure.figureType == FigureType.COMPOSITIONS) {
            item { Spacer(Modifier.height(35.dp)) }
        }
        item { GeneratePlotButton(visualizeModel, context, showAd) }
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
        item { Spacer(Modifier.height(80.dp)) }
    }
}


fun deleteFromGallery(visualizeModel: VisualizeModel, context: Context) {
    visualizeModel.viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                visualizeModel.deleteImageById(visualizeModel.galleryId)
                deleteImageFromUri(context, Uri.parse(visualizeModel.galleryURI))
            }
            visualizeModel.galleryURI = ""
            visualizeModel.galleryId = 0
            visualizeModel.isFromGallery = false
        } catch (e: Exception) {
            Log.e("DeleteFromGallery", "Failed to delete image", e)
        }
    }
}


fun deleteImageFromUri(context: Context, uri: Uri): Boolean {
    return try {
        val contentResolver: ContentResolver = context.contentResolver
        val rowsDeleted = contentResolver.delete(uri, null, null)
        rowsDeleted > 0 // Returns true if the deletion was successful
    } catch (e: Exception) {
        Log.e("DeleteImage", "Failed to delete image: $uri", e)
        false
    }
}

private fun setWallpaperAfterAd(visualizeModel: VisualizeModel, context: Context) {
    val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
    if (androidBitmap != null) {
        setWallpaper(
            context,
            visualizeModel
        ) // Set wallpaper after the ad is dismissed or if the ad is not loaded
    } else {
        Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
    }
}

fun saveToGallery(visualizeModel: VisualizeModel, context: Context) {
    visualizeModel.isSavingLoading = true
    var uri: Uri? = null
    visualizeModel.viewModelScope.launch {
        val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
        if (androidBitmap != null) {
            uri = saveBitmapToGallery(
                context,
                androidBitmap,
                visualizeModel.selectedFigure.key
            )
        }

        val imageEntity = visualizeModel.temporalImageEntity
            .setUri(uri?.toString() ?: "")
            .build()
        visualizeModel.addImage(imageEntity)
        loadSavedImage(visualizeModel, imageEntity, context)
        visualizeModel.isSavingLoading = false
    }
}


fun saveImageBitmapToCache(imageBitmap: ImageBitmap, context: Context): Uri? {
    // Create a temporary file in the cache directory
    val file = File(context.cacheDir, "shared_image.png")
    file.outputStream().use { outputStream ->
        // Convert ImageBitmap to Bitmap and compress it to PNG format
        val bitmap = imageBitmap.asAndroidBitmap()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    // Return a content URI for the file using FileProvider
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

