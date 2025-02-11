package com.aleespa.randomsquare.screens

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.BackgroundColors
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.LatexMathView
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.loadBitmapFromFile
import com.aleespa.randomsquare.tools.loadSavedImage
import com.aleespa.randomsquare.tools.saveBitmapToGallery
import com.aleespa.randomsquare.tools.setWallpaper
import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun Visualize(visualizeModel: VisualizeModel,
              navController: NavHostController,
              mInterstitialAd: InterstitialAd?) {
    val context = LocalContext.current
    BackHandler {
        navController.navigate(BottomBarScreen.Browse.route)
    }

    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if ((savedBitmap != null).and(visualizeModel.isFromGallery.not())) {
        visualizeModel.imageBitmapState = savedBitmap?.asImageBitmap()
    }
    if (visualizeModel.showAspectRatioDialog) {
        AspectRatioDialog(
            visualizeModel,
            onDismiss = { visualizeModel.showAspectRatioDialog = false },
            onConfirm = {
                visualizeModel.showAspectRatioDialog = false
                setWallpaperAfterAd(visualizeModel, context)
            }
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        item { Spacer(Modifier.height(40.dp)) }
        item { TitleFigure(visualizeModel, context) }
        item { VisualizeBox(visualizeModel) }
        item { GeneratePlotButton(visualizeModel, context) }
        item { BackgroundColorButtons(visualizeModel) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
@Composable
fun TitleFigure(visualizeModel: VisualizeModel, context: Context) {
    // State to manage the visibility of the dropdown menu
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp), // Optional padding
        contentAlignment = Alignment.Center // Centers the content inside the Box
    ) {
        // Text for the title
        Text(
            text = stringResource(visualizeModel.selectedFigure.resourceStringId),
            style = TextStyle(
                fontFamily = parkinsansFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center // Center the text
        )

        // Three-dot button aligned to the right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd) // Align to the right
                .clickable { showMenu = true } // Open the menu on click
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert, // Three-dot icon
                contentDescription = "Options",
                modifier = Modifier.size(28.dp)
            )

            // Dropdown menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        if (visualizeModel.isFromGallery.not()){
                            saveToGallery(visualizeModel, context)
                        } else {
                            deleteFromGallery(visualizeModel, context)
                        }
                        showMenu = false
                    }
                ) {
                    if (visualizeModel.isFromGallery.not()){
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Align icon and text vertically
                            horizontalArrangement = Arrangement.Start, // Align content to the start
                            modifier = Modifier.fillMaxWidth() // Ensure the Row takes full width
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add, // Replace with your desired icon
                                contentDescription = "Save Icon",
                                modifier = Modifier.size(20.dp) // Adjust icon size as needed
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                            Text(
                                text = stringResource(id = R.string.save),
                                modifier = Modifier.weight(1f) // Take up remaining space
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Align icon and text vertically
                            horizontalArrangement = Arrangement.Start, // Align content to the start
                            modifier = Modifier.fillMaxWidth() // Ensure the Row takes full width
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete, // Replace with your desired icon
                                contentDescription = "Save Icon",
                                modifier = Modifier.size(20.dp) // Adjust icon size as needed
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                            Text(
                                text = stringResource(id = R.string.delete_from_gallery),
                                modifier = Modifier.weight(1f) // Take up remaining space
                            )
                        }
                    }
                                    }
                DropdownMenuItem(
                    onClick = {
                        visualizeModel.showAspectRatioDialog = true
                        showMenu = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate, // Replace with your desired icon
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(20.dp) // Adjust icon size as needed
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                    Text(
                        text = stringResource(id = R.string.set_wallpaper),
                        modifier = Modifier.weight(1f) // Take up remaining space
                    )
                }
                DropdownMenuItem(
                    onClick = {
                        val imageUri = visualizeModel.imageBitmapState?.let { saveImageBitmapToCache(it, context) }

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png" // Set MIME type for images
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, R.string.share_text.toString()))
                        showMenu = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, // Replace with your desired icon
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(20.dp) // Adjust icon size as needed
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                    Text(
                        text = stringResource(id = R.string.share),
                        modifier = Modifier.weight(1f) // Take up remaining space
                    )
                }
                DropdownMenuItem(
                    onClick = {
                        visualizeModel.showInfo = !visualizeModel.showInfo
                        showMenu = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info, // Replace with your desired icon
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(20.dp) // Adjust icon size as needed
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
                    Text(
                        text = stringResource(id = R.string.more_info),
                        modifier = Modifier.weight(1f) // Take up remaining space
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
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}


fun deleteFromGallery(visualizeModel: VisualizeModel, context: Context){
    visualizeModel.viewModelScope.launch {try {
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
                setWallpaperAfterAd(visualizeModel, context)
            }
        )
    }
}

private fun setWallpaperAfterAd(visualizeModel: VisualizeModel, context: Context) {
    val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
    if (androidBitmap != null) {
        setWallpaper(context, visualizeModel) // Set wallpaper after the ad is dismissed or if the ad is not loaded
    } else {
        Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
    }
}

fun saveToGallery(visualizeModel: VisualizeModel, context: Context){
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
        val imageUri = visualizeModel.imageBitmapState?.let { saveImageBitmapToCache(it, context) }

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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly, // Distributes items evenly
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(26.dp)
                .clickable {
                    visualizeModel.showAspectRatioDialog = true
                }
        )

        // Center Button
        ExtendedFloatingActionButton(
            elevation = FloatingActionButtonDefaults.elevation(10.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = {
                generateNewPlot(visualizeModel, context)
            },
            icon = {
                Icon(
                    Icons.Default.Casino,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.generate),
                    textAlign = TextAlign.Center,
                )
            }
        )

        // Right Icon (Favorite/Unfavorite)
        Icon(
            imageVector = if (visualizeModel.isFromGallery.not()) Icons.Default.StarBorder else Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(26.dp)
                .clickable {
                    if (visualizeModel.isFromGallery.not()) {
                        saveToGallery(visualizeModel, context)
                    } else {
                        deleteFromGallery(visualizeModel, context)
                    }
                }
        )

        Spacer(Modifier.width(16.dp))
    }
}


@Composable
fun BackgroundColorButtons(visualizeModel: VisualizeModel){
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        var backgroundOptions = BackgroundColors.entries.toTypedArray();

        itemsIndexed(backgroundOptions) { index, backgroundColor ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(backgroundColor.color, shape = RoundedCornerShape(8.dp))
                    .border(
                        width = if (visualizeModel.bgColor == backgroundColor.color) 3.dp else 0.dp,
                        color = if (visualizeModel.bgColor == backgroundColor.color) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        visualizeModel.bgColor = backgroundColor.color
                        visualizeModel.isDarkMode = backgroundColor.type == "Dark"
                    },
                contentAlignment = Alignment.Center

            ){}
        }
    }
}


fun selectColors(visualizeModel: VisualizeModel) {
    visualizeModel.showColorDialog = true
}



@Composable
fun VisualizeSettingsButtons(
    visualizeModel: VisualizeModel,
    context: Context,
    mInterstitialAd: InterstitialAd?
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