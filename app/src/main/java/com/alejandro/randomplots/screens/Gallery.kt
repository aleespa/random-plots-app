package com.alejandro.randomplots.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.alejandro.randomplots.BottomBarScreen
import com.alejandro.randomplots.Figures
import com.alejandro.randomplots.R
import com.alejandro.randomplots.data.VisualizeModel
import com.alejandro.randomplots.tools.readTexAssets
import com.alejandro.randomplots.tools.setBitmapToCache


@Composable
fun Gallery(visualizeModel: VisualizeModel = viewModel(),
            navController: NavHostController) {
    val context = LocalContext.current
    val folderPath = "Pictures/RandomPlots"
    RandomGalleryTopBar(navController, folderPath, context, visualizeModel)
}


fun getImagesFromFolder(context: Context, folderPath: String): List<Pair<String, Uri>> {
    val images = mutableListOf<Pair<String, Uri>>()

    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN
    )
    val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
    val selectionArgs = arrayOf("%$folderPath%")
    val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

    context.contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val contentUri: Uri = Uri.withAppendedPath(uri, id.toString())
            images.add(name to contentUri)
        }
    }

    return images
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGalleryTopBar(navController: NavHostController,
                        folderPath: String,
                        context: Context,
                        visualizeModel: VisualizeModel) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        stringResource(id=R.string.saved_images),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(BottomBarScreen.Visualize.route)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        ScrollContent(innerPadding, folderPath, context, navController, visualizeModel)
    }
}

@Composable
fun ScrollContent(innerPadding: PaddingValues,
                  folderPath: String,
                  context: Context,
                  navController: NavHostController,
                  visualizeModel: VisualizeModel) {
    val images = remember(folderPath) { getImagesFromFolder(context, folderPath) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Display three items per row
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding // Optional: Add padding between items
    ) {
        items(images) { (imageName, imageFile) ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageFile)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable {
                        setBitmapToCache(
                            context,
                            loadImage(context, imageFile),
                            "cache_front.png"
                        )
                        val figureKey = extractKeyFromFilename(imageName)
                        visualizeModel.selectedOption = Figures.fromKey(figureKey)
                        visualizeModel.latexString = readTexAssets(context,
                            visualizeModel.selectedOption.key)
                        navController.navigate(
                            BottomBarScreen.Visualize.route
                        )
                    }
            )
        }
    }
}

fun extractKeyFromFilename(filename: String): String {
    val regex = Regex("^(.*?_.*?_).*")
    return regex.matchEntire(filename)?.groups?.get(1)?.value?.removeSuffix("_") ?: ""
}


fun loadImage(context: Context, imageFile: Uri): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, imageFile)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.setTargetSampleSize(
            calculateSampleSize(
                info.size.width,
                info.size.height,
                800,
                800
            )
        )
    }
}

fun calculateSampleSize(
    originalWidth: Int,
    originalHeight: Int,
    requiredWidth: Int,
    requiredHeight: Int
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