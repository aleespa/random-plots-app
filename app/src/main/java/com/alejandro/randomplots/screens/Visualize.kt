package com.alejandro.randomplots.screens

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alejandro.randomplots.Figures
import com.alejandro.randomplots.R
import com.alejandro.randomplots.data.ImageEntity
import com.alejandro.randomplots.data.VisualizeModel
import com.alejandro.randomplots.tools.LatexMathView
import com.alejandro.randomplots.tools.generateNewPlot
import com.alejandro.randomplots.tools.loadBitmapFromFile
import com.alejandro.randomplots.tools.loadSavedImage
import com.alejandro.randomplots.tools.readTexAssets
import com.alejandro.randomplots.tools.saveBitmapToGallery
import com.alejandro.randomplots.tools.setWallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun Visualize(visualizeModel: VisualizeModel = viewModel()) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    BackHandler {
        showDialog = true // Show confirmation dialog
    }

    if (showDialog) {
        AlertDialog(
            title = { Text("Exit") },
            text = { Text("Are you sure you want to exit?") },
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    (context as? Activity)?.finish()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if (visualizeModel.isFromGallery){
        visualizeModel.imageBitmapState = loadImage(context,
            Uri.parse(visualizeModel.galleryURI)).asImageBitmap()
    }
    else if (savedBitmap != null) {
            visualizeModel.imageBitmapState = savedBitmap.asImageBitmap()
    }

    val options = Figures.entries.map { it }
    LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(Modifier.height(10.dp)) }
        item { PlotDrawer(visualizeModel, context, options) }
        item { VisualizeBox(visualizeModel) }
        item { GeneratePlotButton(visualizeModel, context)}
        item { VisualizeSettingsButtons(visualizeModel, context) }
        item { Spacer(Modifier.height(80.dp)) }
    }

}
@Composable
fun PlotDrawer(
    visualizeModel: VisualizeModel,
    context: Context,
    options: List<Figures>
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        items(options) { option ->
            val painterIcon = painterResource(id = option.iconResourceId)
            var containerColor = MaterialTheme.colorScheme.secondaryContainer
            var elevation = 10.dp
            val selected = visualizeModel.selectedFigure
            val isSelected = option == selected
            if (isSelected) {
                containerColor = MaterialTheme.colorScheme.inversePrimary
                elevation = 12.dp
            }

            ElevatedCard(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(0.70f)
                    .clickable {
                        if (visualizeModel.selectedFigure != option) {
                            visualizeModel.selectedFigure = option
                            visualizeModel.latexString = readTexAssets(
                                context,
                                visualizeModel.selectedFigure.key
                            )
                            generateNewPlot(visualizeModel, context)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.height(70.dp), // Ensure consistent height for the icon
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            painter = painterIcon,
                            contentDescription = null,
                            modifier = Modifier.size(65.dp) // Fixed size
                        )
                    }
                    // Wrap Text in a Box with fixed height
                    Box(
                        modifier = Modifier.height(20.dp), // Ensure consistent height
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = option.resourceStringId),
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            lineHeight = 10.sp
                        )
                    }
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
    context: Context){

    VisualizeOptionsButtons(
        icon=Icons.Rounded.AddPhotoAlternate,
        bottomText = stringResource(id = R.string.set_wallpaper))
    {
        val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
        if (androidBitmap != null) {
            setWallpaper(context, androidBitmap)
        }
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
                .padding(start = 170.dp)  // Adjust padding to fine-tune position
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
fun BackgroundSelectionDialog(visualizeModel: VisualizeModel) {
    var darkColors = listOf(
        Color(0, 0, 0),
        Color(64, 11, 0),
        Color(0, 30, 26 ),
        Color( 0, 13, 30))
    var lightColors = listOf(
        Color(255, 255, 255),
        Color(244, 240, 231),
        Color(234, 250, 241),
        Color(251, 238, 230))
    val backgroundOptions = if (visualizeModel.isDarkMode) {
        darkColors
    } else {
        lightColors
    }
    if (visualizeModel.showColorDialog) {
        AlertDialog(
            onDismissRequest = { visualizeModel.showColorDialog = false },
            title = {
                Text(text = "Select Background Mode", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                visualizeModel.isDarkMode = false
                                visualizeModel.bgColor = lightColors[0]
                                      },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!visualizeModel.isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Light Mode")
                        }
                        Button(
                            onClick = {
                                visualizeModel.isDarkMode = true
                                visualizeModel.bgColor = darkColors[0]
                                      },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (visualizeModel.isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Dark Mode")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {


                        itemsIndexed(backgroundOptions) { index, color ->
                            if (visualizeModel.bgColor == color){
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(color, shape = RoundedCornerShape(8.dp))
                                        .border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            visualizeModel.bgColor = color
                                        },
                                    contentAlignment = Alignment.Center

                                    ){}
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(color, shape = RoundedCornerShape(8.dp))
                                        .clickable {
                                            visualizeModel.bgColor = color
                                        },
                                    contentAlignment = Alignment.Center
                                    ){}
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { visualizeModel.showColorDialog = false }) {
                    Text(text = "Accept")
                }
            }
        )
    }
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

fun loadImage(context: Context, imageFile: Uri): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, imageFile)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.setTargetSampleSize(
            calculateSampleSize(
                info.size.width,
                info.size.height,
                1200,
                1200
            )
        )
    }
}