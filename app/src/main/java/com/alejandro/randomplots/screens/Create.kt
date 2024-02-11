package com.alejandro.randomplots.screens

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alejandro.randomplots.R
import com.alejandro.randomplots.create.generateRandomPlot
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView

@Composable
fun Create() {
    var rotated by remember {
        mutableStateOf(false)
    }
    val imageBitmapState = remember { mutableStateOf<ImageBitmap?>(null) }
    val latexString = remember {
        mutableStateOf<String>("")
    }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val textColor = if (isSystemInDarkTheme) Color.WHITE else Color.BLACK
    val context = LocalContext.current
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
                .width(IntrinsicSize.Max)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { rotated = !rotated },
        ) {
            if (!rotated){
                ImageWithNullFallback(imageBitmapState.value)
            }else{
                LatexMathView(
                    latexString = latexString.value,
                    textColor = textColor
                )
            }
        }
        Spacer(Modifier.height(70.dp))
        Column {
            ElevatedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    val result  = generateRandomPlot()
                    imageBitmapState.value = result.first
                    latexString.value = result.second}) {
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
                ExtendedFloatingActionButton(onClick = {
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
                ExtendedFloatingActionButton(onClick = {
                    val androidBitmap = imageBitmapState.value?.asAndroidBitmap()
                    setWallpaper(context, androidBitmap)
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
fun LatexMathView(latexString: String, textColor: Int) {
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


@Preview
@Composable
fun GreetingPreview() {
    Create()
}

fun setWallpaper(context: Context,
                 bitmap: Bitmap?) {
    if (bitmap == null){
        Toast.makeText(context, R.string.generate_img_first, Toast.LENGTH_SHORT).show()
    } else {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)
            Toast.makeText(context, R.string.wallpaper_set, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, R.string.wallpaper_fail, Toast.LENGTH_SHORT).show()
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
