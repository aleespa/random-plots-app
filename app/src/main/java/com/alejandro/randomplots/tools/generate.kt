package com.alejandro.randomplots.tools

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.alejandro.randomplots.R
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64


suspend fun generateRandomPlot(isDarkMode: Boolean, script: String = "spirograph"):
        Pair<ImageBitmap?, String> = withContext(Dispatchers.IO) {
    val py = Python.getInstance()
    val mainModule = py.getModule("main")
    val result = mainModule.callAttr("generate", isDarkMode, script).asList()

    val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

    return@withContext Pair(
        BitmapFactory
            .decodeByteArray(imageBytes, 0, imageBytes.size)
            ?.asImageBitmap(),
        result[1].toString()
    )
}


fun setWallpaper(context: Context, bitmap: Bitmap?) {
    if (bitmap == null) {
        Toast.makeText(context, R.string.generate_img_first, Toast.LENGTH_SHORT).show()
        return
    }

    // Move wallpaper setting to a background thread to avoid blocking the main thread
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)

            // Show success message on the main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.wallpaper_set, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.wallpaper_fail, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.wallpaper_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


fun saveBitmapToGallery(context: Context,
                        bitmap: Bitmap,
                        displayName: String = "Random_plot_${System.currentTimeMillis()}.png") {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/RandomPlots")
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        context.contentResolver.openOutputStream(it).use { outputStream ->
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String){
    try {
        deleteFileIfExists(context, filename)
        val file = File(context.filesDir, filename)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
fun deleteFileIfExists(context: Context, filename: String) {
    val file = File(context.filesDir, filename)
    if (file.exists()) {
        val deleted = file.delete() // Try to delete the file
        if (!deleted) {
            Log.e("FileDebug", "Failed to delete file: $filename")
        }
    }
}
fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
    return try {
        val file = File(context.filesDir, filename)
        if (file.exists()) {
            Log.d("","File exists")
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null // Return null if the file doesn't exist
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}