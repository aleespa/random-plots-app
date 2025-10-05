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
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.ImageEntity
import com.aleespa.randomsquare.data.VisualizeModel
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextUInt


fun generateRandomPlot(visualizeModel: VisualizeModel): ImageBitmap? {
    val colormapColors = visualizeModel.selectedColormap.colorlist.toTypedArray().map { color ->
            intColorToHexWithoutAlpha(color)
        }

    val py = Python.getInstance()
    val mainModule = py.getModule("main")

    // Correct way to get the list function and call it
    val listFunction = py.builtins.get("list")
    val pythonList = listFunction?.call(colormapColors.toTypedArray())

    val imageBytes = mainModule.callAttr(
        "generate",
        visualizeModel.randomSeed,
        colorToHexWithoutAlpha(Color(visualizeModel.bgColor)),
        visualizeModel.selectedFigure.key,
        pythonList
    ).toJava(ByteArray::class.java)

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
}


fun loadImage(
    context: Context, imageFile: Uri, targetWidth: Int = 1200, targetHeight: Int = 1200
): ImageBitmap? {
    return try {
        // Open InputStream using ContentResolver
        context.contentResolver.openInputStream(imageFile)?.use { inputStream ->
            // Load the image dimensions first to calculate scaling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)

            // Calculate sample size
            options.inSampleSize =
                calculateSampleSize(options.outWidth, options.outHeight, targetWidth, targetHeight)
            options.inJustDecodeBounds = false

            // Decode the scaled bitmap
            context.contentResolver.openInputStream(imageFile)?.use { scaledInputStream ->
                BitmapFactory.decodeStream(scaledInputStream, null, options)?.asImageBitmap()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null in case of an error
    }
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
                bitmap, resolution[0], resolution[1], Color(visualizeModel.bgColor)
            )
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Create a URI for the file
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )

        // Create an Intent to open the default wallpaper app
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the Intent
        context.startActivity(
            Intent.createChooser(
                intent, context.getString(R.string.select_wallpaper_app)
            )
        )

    } catch (_: IOException) {
        Toast.makeText(context, R.string.wallpaper_fail, Toast.LENGTH_SHORT).show()
    }
}


fun saveBitmapToGallery(
    context: Context, bitmap: Bitmap, prefix: String
): Uri? {
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


fun setBitmapToCache(context: Context, bitmap: Bitmap, filename: String) {
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
            Log.d("", "File exists")
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
    if (visualizeModel.userSeed.not()
        or (visualizeModel.selectedFigure.figureType != FigureType.COMPOSITIONS)) {
        visualizeModel.randomSeed = generate32BitSeed()
        visualizeModel.userSeed = false
    }
    CoroutineScope(Dispatchers.Main).launch {
        visualizeModel.temporalImageEntity =
            ImageEntity.Builder().setImageType(visualizeModel.selectedFigure.key)
                .setIsDarkMode(isColorDark(visualizeModel.bgColor))
                .setTimestamp(System.currentTimeMillis()).setRandomSeed(visualizeModel.randomSeed)
                .setBackgroundColor(fromColor(Color(visualizeModel.bgColor)))
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

fun loadSavedImage(
    visualizeModel: VisualizeModel, image: ImageEntity, context: Context
) {
    visualizeModel.isFromGallery = true
    visualizeModel.galleryURI = image.uri
    visualizeModel.galleryId = image.id
    visualizeModel.bgColor = image.backgroundColor
    visualizeModel.randomSeed = image.randomSeed

    val figureKey = image.imageType
    visualizeModel.selectedFigure = Figures.fromKey(figureKey)
    visualizeModel.latexString = readTexAssets(
        context, visualizeModel.selectedFigure.key
    )
    visualizeModel.imageBitmapState = loadImage(context, Uri.parse(image.uri))
    visualizeModel.showInfo = false
}

fun generate32BitSeed(): Long {
    return Random.nextLong(0, Long.MAX_VALUE)
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
        targetWidth.toFloat() / originalBitmap.width, targetHeight.toFloat() / originalBitmap.height
    )
    val scaledWidth = (originalBitmap.width * scale).toInt()
    val scaledHeight = (originalBitmap.height * scale).toInt()
    val left = (targetWidth - scaledWidth) / 2
    val top = (targetHeight - scaledHeight) / 2.3

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


fun calculateSampleSize(
    originalWidth: Int, originalHeight: Int, requiredWidth: Int, requiredHeight: Int
): Int {
    var inSampleSize = 1

    if (originalHeight > requiredHeight || originalWidth > requiredWidth) {
        val halfHeight = originalHeight / 2
        val halfWidth = originalWidth / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= requiredHeight && halfWidth / inSampleSize >= requiredWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}