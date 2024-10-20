package com.alejandro.randomplots.screens

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Composable
fun Gallery() {
    var state by remember { mutableIntStateOf(0) }
    val titles = listOf("Grid", "List")
    val context = LocalContext.current

    Column(
        modifier =  Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        ImageList(context, "Pictures/RandomPlots", state)
    }
}
@Composable
fun ImageList(context: Context, folderPath: String, state: Int) {
    val images = remember(folderPath) { getImagesFromFolder(context, folderPath) }
    if (state == 0){
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Display three items per row
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp) // Optional: Add padding between items
        ) {
            items(images) { imageFile ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(images) { imageFile ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}


fun getImagesFromFolder(context: Context, folderPath: String): List<Uri> {
    val images = mutableListOf<Uri>()

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
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val contentUri: Uri = Uri.withAppendedPath(uri, id.toString())
            images.add(contentUri)
        }
    }

    return images
}

// Helper function to convert URI string to URI
fun String.toUri(): Uri = Uri.parse(this)