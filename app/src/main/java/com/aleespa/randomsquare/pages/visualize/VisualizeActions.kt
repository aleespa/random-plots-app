package com.aleespa.randomsquare.pages.visualize

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.loadSavedImage
import com.aleespa.randomsquare.tools.saveBitmapToGallery
import com.aleespa.randomsquare.tools.setWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
        rowsDeleted > 0
    } catch (e: Exception) {
        Log.e("DeleteImage", "Failed to delete image: $uri", e)
        false
    }
}

fun setWallpaperAfterAd(visualizeModel: VisualizeModel, context: Context) {
    val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
    if (androidBitmap != null) {
        setWallpaper(
            context,
            visualizeModel
        )
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
            .setImageType(visualizeModel.selectedFigure.key) // Ensure correct type
            .setUri(uri?.toString() ?: "")
            .build()
        visualizeModel.addImage(imageEntity)
        loadSavedImage(visualizeModel, imageEntity, context)
        visualizeModel.isSavingLoading = false
    }
}

fun saveImageBitmapToCache(imageBitmap: ImageBitmap, context: Context): Uri? {
    val file = File(context.cacheDir, "shared_image.png")
    file.outputStream().use { outputStream ->
        val bitmap = imageBitmap.asAndroidBitmap()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
