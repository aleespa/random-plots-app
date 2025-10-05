package com.aleespa.randomsquare.pages.visualize

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.parkinsansFontFamily
import java.io.File


@Composable
fun HeaderSection(visualizeModel: VisualizeModel, context: Context) {
    var showMenu by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp), // Optional padding
        contentAlignment = Alignment.Center // Centers the content inside the Box
    ) {
        TitleText(stringResource(visualizeModel.selectedFigure.resourceStringId))
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd) // Align to the right
                .clickable { showMenu = true } // Open the menu on click
        ) {
            ThreeDotsDropDownMenu(
                visualizeModel,
                context,
                showMenu
            ) { showMenu = false }
        }
    }
}


@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = parkinsansFontFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}


@Composable
fun ThreeDotsDropDownMenu(
    visualizeModel: VisualizeModel,
    context: Context,
    showMenu: Boolean,
    onDismiss: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "Options",
        modifier = Modifier.size(28.dp)
    )
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onDismiss() }
    ) {
        DropdownMenuItem(
            text = {
                if (visualizeModel.isFromGallery.not())
                    Text(stringResource(id = R.string.save))
                else Text(stringResource(id = R.string.delete_from_gallery))
            },
            leadingIcon = {
                if (visualizeModel.isFromGallery.not()) DropDownMenuIcon(Icons.Default.Add)
                else DropDownMenuIcon(Icons.Default.Delete)
            },
            onClick = {
                if (visualizeModel.isFromGallery.not()) {
                    saveToGallery(visualizeModel, context)
                } else {
                    deleteFromGallery(visualizeModel, context)
                }
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.set_wallpaper)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.AddPhotoAlternate) },
            onClick = {
                visualizeModel.showAspectRatioDialog = true
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.share)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.Share) },
            onClick = {
                shareImageBitmap(visualizeModel, context)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.more_info)) },
            leadingIcon = { DropDownMenuIcon(Icons.Default.Info) },
            onClick = {
                visualizeModel.showInfo = !visualizeModel.showInfo
                onDismiss()
            }
        )
    }
}


@Composable
fun DropDownMenuIcon(icon: ImageVector) {
    Icon(
        imageVector = icon, // Replace with your desired icon
        contentDescription = "Save Icon",
        modifier = Modifier.size(20.dp) // Adjust icon size as needed
    )
}


fun shareImageBitmap(visualizeModel: VisualizeModel, context: Context) {
    val imageUri = visualizeModel.imageBitmapState?.let { saveImageBitmapToCache(it, context) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png" // Set MIME type for images
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, R.string.share_text.toString()))
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
