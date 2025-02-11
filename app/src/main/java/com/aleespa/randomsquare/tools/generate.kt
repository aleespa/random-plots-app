package com.aleespa.randomsquare.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.ImageEntity
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.screens.loadImage
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextUInt

fun colorToHexWithoutAlpha(color: Color): String {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    return String.format("#%02X%02X%02X", red, green, blue)
}

fun generateRandomPlot(visualizeModel: VisualizeModel):
        ImageBitmap? {
    val py = Python.getInstance()
    val mainModule = py.getModule("main")
    val result = mainModule.callAttr(
        "generate",
        visualizeModel.randomSeed,
        visualizeModel.isDarkMode,
        colorToHexWithoutAlpha(visualizeModel.bgColor),
        visualizeModel.selectedFigure.key)

    val imageBytes = Base64.getDecoder().decode(result.toString().toByteArray())

    return  BitmapFactory
            .decodeByteArray(imageBytes, 0, imageBytes.size)
            ?.asImageBitmap()
}


fun setWallpaper(context: Context, visualizeModel: VisualizeModel) {
    var bitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
    if (bitmap == null) {
        Toast.makeText(context, R.string.generate_img_first, Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val file = File(context.cacheDir, "temp_wallpaper.png")
        val outputStream = FileOutputStream(file)
        var resolution = getScreenResolution(context)
        if (visualizeModel.toFitAspectRatio) {
            bitmap = convertToAspectRatio(
                bitmap,
                resolution[0],
                resolution[1],
                visualizeModel.bgColor
            )
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Create a URI for the file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Create an Intent to open the default wallpaper app
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the Intent
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.select_wallpaper_app)))

    } catch (_: IOException) {
        Toast.makeText(context, R.string.wallpaper_fail, Toast.LENGTH_SHORT).show()
    }
}



fun saveBitmapToGallery(context: Context,
                        bitmap: Bitmap,
                        prefix: String) : Uri? {
    val displayName = "${prefix}_${System.currentTimeMillis()}.png"
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
            }
        }
    }
    return uri
}


fun setBitmapToCache(context: Context, bitmap: Bitmap, filename: String){
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

fun generateNewPlot(visualizeModel: VisualizeModel, context: Context) {
    visualizeModel.loadingPlotGenerator = true
    visualizeModel.showInfo = false
    visualizeModel.randomSeed = generate32BitSeed().toLong()
    CoroutineScope(Dispatchers.Main).launch {
        visualizeModel.temporalImageEntity = ImageEntity.Builder()
            .setImageType(visualizeModel.selectedFigure.key)
            .setIsDarkMode(visualizeModel.isDarkMode)
            .setTimestamp(System.currentTimeMillis())
            .setRandomSeed(visualizeModel.randomSeed)
            .setBackgroundColor(fromColor(visualizeModel.bgColor))
        try {
            val result = withContext(Dispatchers.Default) {
                generateRandomPlot(visualizeModel)
            }
            val androidBitmap = result?.asAndroidBitmap()
            if (androidBitmap != null) {
                withContext(Dispatchers.IO) {
                    setBitmapToCache(context, androidBitmap, "cache_front.png")
                }
            }
            visualizeModel.imageBitmapState = result
            visualizeModel.latexString = readTexAssets(context, visualizeModel.selectedFigure.key)
            visualizeModel.isFromGallery = false
        } finally {
            visualizeModel.loadingPlotGenerator = false
        }
    }
}

fun loadSavedImage(visualizeModel: VisualizeModel,
                   image: ImageEntity,
                   context: Context) {
    visualizeModel.isFromGallery = true
    visualizeModel.galleryURI = image.uri
    visualizeModel.galleryId = image.id
    visualizeModel.bgColor = toColor(image.backgroundColor)

    val figureKey = image.imageType
    visualizeModel.selectedFigure = Figures.fromKey(figureKey)
    visualizeModel.latexString = readTexAssets(
    context,
    visualizeModel.selectedFigure.key
    )
    visualizeModel.imageBitmapState = loadImage(context,Uri.parse(image.uri))
    visualizeModel.showInfo = false
}

fun generate32BitSeed(): UInt {
    return Random.nextUInt()
}
fun convertToAspectRatio(
    originalBitmap: Bitmap,
    targetWidth: Int,
    targetHeight: Int,
    backgroundColor: Color = Color(0xF124124) // Default color as an example
): Bitmap {
    // Create a blank bitmap with the target size
    val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

    // Create a canvas to draw on the new bitmap
    val canvas = Canvas(resultBitmap)

    // Convert the Compose Color to an integer for use with Paint
    val paint = Paint()
    paint.color = backgroundColor.toArgb() // Convert Compose Color to ARGB integer
    canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)

    // Calculate the scaling factor and position to center the original bitmap
    val scale = min(
        targetWidth.toFloat() / originalBitmap.width,
        targetHeight.toFloat() / originalBitmap.height
    )
    val scaledWidth = (originalBitmap.width * scale).toInt()
    val scaledHeight = (originalBitmap.height * scale).toInt()
    val left = (targetWidth - scaledWidth) / 2
    val top = (targetHeight - scaledHeight) / 2

    // Draw the original bitmap onto the new canvas
    canvas.drawBitmap(
        Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true),
        left.toFloat(),
        top.toFloat(),
        null
    )

    return resultBitmap
}

fun getScreenResolution(context: Context): List<Int> {
    val displayMetrics = DisplayMetrics()
    val display = context.resources.displayMetrics

    // Get the screen width and height in pixels
    val widthPixels = display.widthPixels
    val heightPixels = display.heightPixels

    return listOf<Int>(widthPixels, heightPixels)
}


fun fromColor(color: Color): Int {
    return color.toArgb() // Convert Color to Int (ARGB format)
}

fun toColor(value: Int): Color {
    return Color(value) // Convert Int back to Color
}