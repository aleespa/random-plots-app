package com.aleespa.randomsquare.screens

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.LatexMathView
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.loadBitmapFromFile
import com.aleespa.randomsquare.tools.loadSavedImage
import com.aleespa.randomsquare.tools.readTexAssets
import com.aleespa.randomsquare.tools.saveBitmapToGallery
import com.aleespa.randomsquare.tools.setWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.BottomBarScreen


@Composable
fun Visualize(visualizeModel: VisualizeModel,
              navController: NavHostController) {
    val context = LocalContext.current
    BackHandler {
        navController.navigate(BottomBarScreen.Browse.route)
    }

    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if ((savedBitmap != null).and(visualizeModel.isFromGallery.not())) {
        visualizeModel.imageBitmapState = savedBitmap?.asImageBitmap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        item { Spacer(Modifier.height(20.dp)) }
        item { TitleFigure(visualizeModel) }
        item { VisualizeBox(visualizeModel) }
        item { GeneratePlotButton(visualizeModel, context) }
        item { VisualizeSettingsButtons(visualizeModel, context) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
@Composable
fun TitleFigure(visualizeModel: VisualizeModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp), // Optional padding
        contentAlignment = Alignment.Center // Centers the content inside the Box
    ) {
        Text(
            text = stringResource(visualizeModel.selectedFigure.resourceStringId),
            style = TextStyle(
                fontFamily = parkinsansFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
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
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SwitchWithIconExample() {
    var checked by remember { mutableStateOf(true) }

    Switch(
        checked = checked,
        onCheckedChange = {
            checked = it
        },
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun DeleteFromGalleryButton(visualizeModel: VisualizeModel, context: Context) {
    VisualizeOptionsButtons(
        icon = Icons.Rounded.Delete,
        bottomText = stringResource(id = R.string.delete_from_gallery)
    ) {
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
@Composable
fun SetWallpaperButton(
    visualizeModel: VisualizeModel,
    context: Context
) {
    VisualizeOptionsButtons(
        icon = Icons.Rounded.AddPhotoAlternate,
        bottomText = stringResource(id = R.string.set_wallpaper)
    ) {
        visualizeModel.showAspectRatioDialog = true // Show the dialog
    }

    if (visualizeModel.showAspectRatioDialog) {
        AspectRatioDialog(
            visualizeModel,
            onDismiss = { visualizeModel.showAspectRatioDialog = false },
            onConfirm = {
                visualizeModel.showAspectRatioDialog = false
                val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
                if (androidBitmap != null) {
                    setWallpaper(context, visualizeModel) // Set wallpaper only after user confirms
                }
            }
        )
    }
}

@Composable
fun SaveToGalleryButton(visualizeModel: VisualizeModel, context: Context) {
    var uri: Uri? = null

    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable {
                // Use a coroutine scope for asynchronous operations
                visualizeModel.isSavingLoading = true
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
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (visualizeModel.isSavingLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .aspectRatio(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = stringResource(id = R.string.save),
                modifier = Modifier,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}



@Composable
fun ShareButton(
    visualizeModel: VisualizeModel,
    context: Context) {
    VisualizeOptionsButtons(
        icon = Icons.Rounded.Share,
        bottomText = stringResource(id = R.string.share)
    ){
        // Save the ImageBitmap to a file and get its URI
        val imageUri = visualizeModel.imageBitmapState?.let { saveImageBitmapToCache(it, context) }

        // Create a share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png" // Set MIME type for images
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, R.string.share_text.toString()))
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


@Composable
fun VisualizeOptionsButtons(
    id: Int,
    iconSize: Dp= 30.dp,
    bottomText: String,
    onClick: () -> Unit){
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable {
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {  Icon(
        painter = painterResource(id = id),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(iconSize)
            .aspectRatio(1f))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ){
            Text(
                text = bottomText,
                modifier = Modifier,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        } }
}


@Composable
fun VisualizeOptionsButtons(
    icon: ImageVector,
    bottomText: String,
    onClick: () -> Unit){
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable {
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {  Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(20.dp)
            .aspectRatio(1f))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.TopCenter
        ){
            Text(
                text = bottomText,
                modifier = Modifier,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        } }
}

@Composable
fun GeneratePlotButton(
    visualizeModel: VisualizeModel,
    context: Context
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center // Ensures the button is centered
    ) {
        ExtendedFloatingActionButton(
            elevation = FloatingActionButtonDefaults.elevation(10.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = {
                generateNewPlot(visualizeModel, context)
            }
        ) {
            Text(
                text = stringResource(id = R.string.generate),
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center) // Aligns to the right of the button
                .padding(start = 180.dp)  // Adjust padding to fine-tune position
                .clickable {
                    selectColors(visualizeModel)
                }
        ) {
            Icon(
                Icons.Default.Colorize, // Replace with your icon resource
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp) // Icon size
            )
        }
    }

    BackgroundSelectionDialog(visualizeModel)

}

fun selectColors(visualizeModel: VisualizeModel) {
    visualizeModel.showColorDialog = true
}



@Composable
fun VisualizeSettingsButtons(
    visualizeModel: VisualizeModel,
    context: Context
){
    Row(
        modifier = Modifier
            .height(65.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visualizeModel.isFromGallery.not()){
            SaveToGalleryButton(visualizeModel, context)
        } else {
            DeleteFromGalleryButton(visualizeModel, context)
        }
        SetWallpaperButton(visualizeModel, context)
        MoreInfoButton(visualizeModel)
        ShareButton(visualizeModel, context)
    }
}

@Composable
fun MoreInfoButton(visualizeModel: VisualizeModel) {
    VisualizeOptionsButtons(
        icon = Icons.Rounded.Info,
        bottomText = stringResource(id = R.string.more_info)
    ){
        visualizeModel.showInfo = !visualizeModel.showInfo
    }
}


@Composable
fun VisualizeBox(visualizeModel: VisualizeModel){
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .aspectRatio(1f)
            .padding(10.dp)
            .clickable { visualizeModel.showInfo = !visualizeModel.showInfo },
    ) {
        if (!visualizeModel.showInfo){
            if (visualizeModel.loadingPlotGenerator) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            } else{
                ImageWithNullFallback(visualizeModel.imageBitmapState)
            }

        }else{
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                LatexMathView(
                    latexString = visualizeModel.latexString
                )
            }

        }
    }
}

fun loadImage(
    context: Context,
    imageFile: Uri,
    targetWidth: Int = 1200,
    targetHeight: Int = 1200
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
            options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, targetWidth, targetHeight)
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


val parkinsansFontFamily = FontFamily(
    Font(R.font.parkinsans, FontWeight.Normal),
    Font(R.font.parkinsans_medium, FontWeight.Bold),
    Font(R.font.parkinsans, FontWeight.Normal, FontStyle.Italic)
)