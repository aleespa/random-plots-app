package com.alejandro.randomplots.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alejandro.randomplots.Figures
import com.alejandro.randomplots.R
import com.alejandro.randomplots.data.DatabaseProvider
import com.alejandro.randomplots.data.ImageEntity
import com.alejandro.randomplots.data.VisualizeModel
import com.alejandro.randomplots.tools.LatexMathView
import com.alejandro.randomplots.tools.generateRandomPlot
import com.alejandro.randomplots.tools.loadBitmapFromFile
import com.alejandro.randomplots.tools.readTexAssets
import com.alejandro.randomplots.tools.setBitmapToCache
import com.alejandro.randomplots.tools.saveBitmapToGallery
import com.alejandro.randomplots.tools.saveStringToFile
import com.alejandro.randomplots.tools.setWallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    if (savedBitmap != null) {
        visualizeModel.imageBitmapState = savedBitmap.asImageBitmap()
    }
    val options = Figures.entries.map { it }
    Column (
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier= Modifier.height(35.dp))
        PlotDrawer(visualizeModel, context, options)
        Spacer(Modifier.height(35.dp))
        VisualizeBox(visualizeModel)
        Spacer(Modifier.height(35.dp))
        VisualizeButtons(visualizeModel, context)
    }
}

@Composable
fun PlotDrawer(visualizeModel: VisualizeModel,
               context: Context,
               options: List<Figures>){
    LazyRow(modifier = Modifier
        .fillMaxWidth()
    ) {
        items(options) { option ->
            val painterIcon = painterResource(id = option.iconResourceId)
            var containerColor = MaterialTheme.colorScheme.secondaryContainer
            var elevation = 10.dp
            val selected = visualizeModel.selectedOption
            val isSelected = option == selected
            if (isSelected) {
                containerColor = MaterialTheme.colorScheme.inversePrimary
                elevation = 12.dp
            }
            Spacer(Modifier.width(10.dp))
            ElevatedCard(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                modifier = Modifier
                    .size(75.dp)
                    .aspectRatio(0.75f)
                    .clickable {
                        visualizeModel.selectedOption = option
                        visualizeModel.isRotated = true
                        visualizeModel.latexString = readTexAssets(
                            context,
                            visualizeModel.selectedOption.key
                        )
                    },
            ){
                Column {
                    Spacer(Modifier.height(10.dp))
                    Icon(painter = painterIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth()
                            .size(45.dp)
                            .aspectRatio(1f))
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ){
                        Text(
                            text = stringResource(id = option.resourceStringId),
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            lineHeight = 10.sp,
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
        contentScale = ContentScale.Inside
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
fun DeleteFromGalleryButton(){
    VisualizeOptionsButtons(
        icon=Icons.Rounded.Delete,
        bottomText = stringResource(id = R.string.delete_from_gallery)){}
}
@Composable
fun SetWallpaperButton(
    imageBitmapState: ImageBitmap?,
    context: Context){

    VisualizeOptionsButtons(
        icon=Icons.Rounded.Favorite,
        bottomText = stringResource(id = R.string.set_wallpaper))
    {
        val androidBitmap = imageBitmapState?.asAndroidBitmap()
        if (androidBitmap != null) {
            setWallpaper(context, androidBitmap)
        }
    }
}

@Composable
fun SaveToGalleryButton(visualizeModel: VisualizeModel, context: Context) {
    VisualizeOptionsButtons(
        icon=Icons.Rounded.Add,
        bottomText=stringResource(id = R.string.save)) {
        val androidBitmap = visualizeModel.imageBitmapState?.asAndroidBitmap()
        if (androidBitmap != null) {
            saveBitmapToGallery(
                context,
                androidBitmap,
                visualizeModel.selectedOption.key
            )
        }
        val imageEntity = ImageEntity(
            uri = "test",
            name = "Generated Image",
            timestamp = System.currentTimeMillis())

        CoroutineScope(Dispatchers.IO).launch {
            DatabaseProvider.getDatabase(context).imageDao().insertImage(imageEntity)
        }
    }
}


@Composable
fun ShareButton(
    imageBitmapState: ImageBitmap,
    context: Context) {
    VisualizeOptionsButtons(
        icon = Icons.Rounded.Share,
        bottomText = stringResource(id = R.string.share)
    ){
        // Save the ImageBitmap to a file and get its URI
        val imageUri = saveImageBitmapToCache(imageBitmapState, context)

        // Create a share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png" // Set MIME type for images
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Launch the share intent
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
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
    context: Context,
    isDarkTheme: Boolean
    ){
    ExtendedFloatingActionButton(
        elevation = FloatingActionButtonDefaults.elevation(10.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = {

            CoroutineScope(Dispatchers.Main).launch {
                visualizeModel.loading = true
                visualizeModel.isRotated = false
                delay(1)
                val result = generateRandomPlot(isDarkTheme, visualizeModel.selectedOption.key)
                val androidBitmap = result?.asAndroidBitmap()
                if (androidBitmap != null) {
                    setBitmapToCache(context, androidBitmap,
                        "cache_front.png")
                }
                visualizeModel.imageBitmapState = result
                visualizeModel.latexString = readTexAssets(context,
                    visualizeModel.selectedOption.key)
                visualizeModel.loading = false
            }}) {
        Text(
            text = stringResource(id = R.string.generate),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VisualizeButtons(
    visualizeModel: VisualizeModel,
    context: Context
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        GeneratePlotButton(visualizeModel, context, isSystemInDarkTheme())
        Spacer(Modifier.height(15.dp))
        Row(
            modifier = Modifier.height(65.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaveToGalleryButton(visualizeModel, context)
            SetWallpaperButton(visualizeModel.imageBitmapState, context)
            if (visualizeModel.isImageFromGallery){
                DeleteFromGalleryButton()
            }
            ShareButton(visualizeModel.imageBitmapState!!, context)
        }
    }
}


@Composable
fun VisualizeBox(visualizeModel: VisualizeModel){
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(10.dp)
            .clickable { visualizeModel.isRotated = !visualizeModel.isRotated },
    ) {
        if (!visualizeModel.isRotated){
            if (visualizeModel.loading) {
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