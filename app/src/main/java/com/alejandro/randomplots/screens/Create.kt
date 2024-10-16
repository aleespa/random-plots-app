package com.alejandro.randomplots.screens

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alejandro.randomplots.R
import com.alejandro.randomplots.create.generateRandomPlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun Create() {

    Log.d("","Create ")
    val isDarkTheme = isSystemInDarkTheme()
    var rotated by remember {
        mutableStateOf(false)
    }
    var loading by remember {
        mutableStateOf(false)
    }

    val imageBitmapState = remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val latexString = remember {
        mutableStateOf<String>("")
    }
    val context = LocalContext.current
    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if (savedBitmap != null) {
        imageBitmapState.value = savedBitmap.asImageBitmap()
    }
    Column (
        modifier = Modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(80.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { rotated = !rotated },
        ) {
            if (!rotated){
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                } else{
                    ImageWithNullFallback(imageBitmapState.value)
                }

            }else{
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    LatexMathView(
                        latexString = latexString.value
                    )
                }

            }
        }
        Spacer(Modifier.height(70.dp))
        Column {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                elevation = FloatingActionButtonDefaults.elevation(10.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    loading = true
                    // Introduce a delay before starting the long-running operation
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1) // Adjust delay time as needed
                        val result = generateRandomPlot(isDarkTheme)
                        val androidBitmap = result.first?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            saveBitmapToFile(context, androidBitmap,
                                "cache_front.png")
                        }
                        imageBitmapState.value = result.first
                        latexString.value = result.second
                        loading = false
                    }}) {
                Text(
                    text = stringResource(id = R.string.generate),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                ExtendedFloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(10.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val androidBitmap = imageBitmapState.value?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            saveBitmapToGallery(context, androidBitmap)
                    }
                }) {
                    Text(
                        text = stringResource(id = R.string.save),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.width(10.dp))
                ExtendedFloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(10.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val androidBitmap = imageBitmapState.value?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            setWallpaper(context, androidBitmap)
                        }
                        Log.d("","Wallpaper set")
                }) {
                    Text(
                        text = stringResource(id = R.string.set_wallpaper),
                        textAlign = TextAlign.Center,
                    )
                }
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
        contentScale = ContentScale.Inside
    )
}

@Composable
fun LatexMathView(latexString: String) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(10.dp),
        factory = { context ->
            JLatexMathView(context).apply {
                visibility = View.VISIBLE
            }
        },
        update = { view ->
            val drawable = JLatexMathDrawable.builder(latexString)
                .textSize(70F)
                .padding(8)
                .align(JLatexMathDrawable.ALIGN_RIGHT)
                .color(textColor)
                .build()
            view.setLatexDrawable(drawable)
        }
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
