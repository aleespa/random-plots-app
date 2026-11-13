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
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.SketchRenderer
import com.aleespa.randomsquare.data.ImageEntity
import com.aleespa.randomsquare.data.VisualizeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

fun generateRandomPlot(
    seed: Long,
    bgColor: Int,
    figure: Figures,
    colormap: Colormaps
): ImageBitmap? {
    val sketchId = figure.sketchId ?: return null

    val bitmap = SketchRenderer.renderBitmap(
        sketchId = sketchId,
        seed = seed,
        width = 1200,
        height = 1200,
        bgColor = bgColor,
        colormap = colormap
    )
    return bitmap?.asImageBitmap()
}

fun generateRandomPlot(
    visualizeModel: VisualizeModel,
    width: Int = -1,
    height: Int = -1
): ImageBitmap? = renderFigure(
    RenderSpec(
        figure = visualizeModel.selectedFigure,
        seed = visualizeModel.randomSeed,
        width = if (width == -1) visualizeModel.imageResolution else width,
        height = if (height == -1) visualizeModel.imageResolution else height,
        bgColor = visualizeModel.bgColor,
        colormap = visualizeModel.selectedColormap,
        fractal = FractalView(
            xCenter = visualizeModel.fractalXCenter,
            yCenter = visualizeModel.fractalYCenter,
            zoom = visualizeModel.fractalZoom,
            iterations = visualizeModel.fractalIterations,
            juliaCX = visualizeModel.juliaCX,
            juliaCY = visualizeModel.juliaCY,
            multibrotD = visualizeModel.multibrotD,
            newtonCoeffs = visualizeModel.newtonCoeffs,
        ),
    )
)

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

fun generateNewPlot(
    visualizeModel: VisualizeModel,
    context: Context,
    randomizeSeed: Boolean = true,
    showAds: Boolean = true,
    width: Int = -1,
    height: Int = -1,
    resetGalleryState: Boolean = true,
    onComplete: () -> Unit = {}
) {
    visualizeModel.loadingPlotGenerator = true
    visualizeModel.isGeneratingRandom = randomizeSeed
    visualizeModel.showInfo = false
    if (randomizeSeed && (visualizeModel.userSeed.not()
                || (visualizeModel.selectedFigure.figureType != FigureType.COMPOSITIONS))
    ) {
        visualizeModel.randomSeed = generate32BitSeed()
        visualizeModel.userSeed = false
        if (visualizeModel.selectedFigure.hasPanZoomView
        ) {
            visualizeModel.resetFractalSettings()
        }
    }
    CoroutineScope(Dispatchers.Main).launch {
        val builder = ImageEntity.Builder()
            .setImageType(visualizeModel.selectedFigure.key)
            .setIsDarkMode(isColorDark(visualizeModel.bgColor))
            .setTimestamp(System.currentTimeMillis())
            .setBackgroundColor(fromColor(Color(visualizeModel.bgColor)))
            .setColormap(visualizeModel.selectedColormap.key)

        if (visualizeModel.selectedFigure.figureType == FigureType.FRACTAL) {
            builder.setRandomSeed(null)
            builder.setFractalXCenter(visualizeModel.fractalXCenter)
            builder.setFractalYCenter(visualizeModel.fractalYCenter)
            builder.setFractalZoom(visualizeModel.fractalZoom)
            builder.setIterations(visualizeModel.fractalIterations)

            when (visualizeModel.selectedFigure) {
                Figures.JULIA -> {
                    builder.setParam1(visualizeModel.juliaCX)
                    builder.setParam2(visualizeModel.juliaCY)
                }

                Figures.MULTIBROT -> {
                    builder.setParam1(visualizeModel.multibrotD)
                }

                Figures.NEWTON -> {
                    if (randomizeSeed) {
                        val rng = java.util.Random(generate32BitSeed())
                        visualizeModel.newtonCoeffs =
                            DoubleArray(9) { (rng.nextInt(19) - 9).toDouble() }
                        // Ensure it's actually degree 8
                        if (visualizeModel.newtonCoeffs[8] == 0.0) visualizeModel.newtonCoeffs[8] =
                            1.0
                    }

                    builder.setParam1(visualizeModel.newtonCoeffs[0])
                    builder.setParam2(visualizeModel.newtonCoeffs[1])
                    builder.setParam3(visualizeModel.newtonCoeffs[2])
                    builder.setParam4(visualizeModel.newtonCoeffs[3])
                    builder.setParam5(visualizeModel.newtonCoeffs[4])
                    builder.setParam6(visualizeModel.newtonCoeffs[5])
                    builder.setParam7(visualizeModel.newtonCoeffs[6])
                    builder.setParam8(visualizeModel.newtonCoeffs[7])
                    builder.setParam9(visualizeModel.newtonCoeffs[8])
                }

                else -> {}
            }
        }

        if (visualizeModel.selectedFigure.hasPanZoomView
        ) {
            builder.setFractalXCenter(visualizeModel.fractalXCenter)
            builder.setFractalYCenter(visualizeModel.fractalYCenter)
            builder.setFractalZoom(visualizeModel.fractalZoom)
        }

        if (visualizeModel.selectedFigure.figureType != FigureType.FRACTAL) {
            builder.setRandomSeed(visualizeModel.randomSeed)
        }

        visualizeModel.temporalImageEntity = builder
        try {
            val result = withContext(Dispatchers.Default) {
                generateRandomPlot(visualizeModel, width, height)
            }
            if (result == null) {
                // Rendering failed (e.g. no OpenGL ES 3.1 for fractals/compositions):
                // keep the current image instead of blanking it, and tell the user.
                Toast.makeText(context, R.string.render_unavailable, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val androidBitmap = result.asAndroidBitmap()
            if (androidBitmap != null) {
                withContext(Dispatchers.IO) {
                    setBitmapToCache(context, androidBitmap, "cache_front.png")
                }
            }
            visualizeModel.imageBitmapState = result
            if (visualizeModel.selectedFigure == Figures.NEWTON) {
                visualizeModel.newtonLatexString = withContext(Dispatchers.Default) {
                    generateNewtonLatex(visualizeModel.newtonCoeffs)
                }
            }
            visualizeModel.latexString = withContext(Dispatchers.IO) {
                readTexAssets(context, visualizeModel.selectedFigure.key)
            }
            if (resetGalleryState) {
                visualizeModel.isFromGallery = false
            }
            onComplete()
        } finally {
            visualizeModel.loadingPlotGenerator = false
        }
    }
}

fun loadSavedImage(
    visualizeModel: VisualizeModel, image: ImageEntity, context: Context
) {
    visualizeModel.isFromGallery = true

    val figure = Figures.fromKey(image.imageType)
    visualizeModel.selectedFigure = figure

    visualizeModel.galleryURI = image.uri
    visualizeModel.galleryId = image.id
    visualizeModel.bgColor = image.backgroundColor
    visualizeModel.randomSeed = image.randomSeed ?: 0L

    if (image.fractalXCenter != null) visualizeModel.fractalXCenter = image.fractalXCenter
    if (image.fractalYCenter != null) visualizeModel.fractalYCenter = image.fractalYCenter
    if (image.fractalZoom != null) visualizeModel.fractalZoom = image.fractalZoom
    if (image.iterations != null) visualizeModel.fractalIterations = image.iterations

    // Map params back to model
    when (figure) {
        Figures.JULIA -> {
            visualizeModel.juliaCX = image.param1 ?: -0.7
            visualizeModel.juliaCY = image.param2 ?: 0.27015
            visualizeModel.updatePolarFromJulia()
        }

        Figures.MULTIBROT -> {
            visualizeModel.multibrotD = image.param1 ?: 3.0
        }

        Figures.NEWTON -> {
            visualizeModel.newtonCoeffs = doubleArrayOf(
                image.param1 ?: -1.0,
                image.param2 ?: 0.0,
                image.param3 ?: 0.0,
                image.param4 ?: 1.0,
                image.param5 ?: 0.0,
                image.param6 ?: 0.0,
                image.param7 ?: 0.0,
                image.param8 ?: 0.0,
                image.param9 ?: 0.0
            )
            visualizeModel.newtonLatexString = generateNewtonLatex(visualizeModel.newtonCoeffs)
            visualizeModel.latexString = readTexAssets(context, figure.key)
        }

        else -> {
            visualizeModel.latexString = readTexAssets(context, figure.key)
        }
    }

    if (image.colormap != null) {
        val foundColormap = Colormaps.values().find { it.key == image.colormap }
        if (foundColormap != null) {
            visualizeModel.selectedColormap = foundColormap
        }
    }

    visualizeModel.imageBitmapState = loadImage(context, Uri.parse(image.uri))
    visualizeModel.showInfo = false
}

fun generate32BitSeed(): Long {
    return Random.nextLong(0, 0xFFFFFFFFL)
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
    DisplayMetrics()
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