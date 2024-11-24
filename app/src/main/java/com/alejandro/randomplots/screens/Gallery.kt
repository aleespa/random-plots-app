package com.alejandro.randomplots.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
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


@Composable
fun Gallery(visualizeModel: VisualizeModel = viewModel(),
            navController: NavHostController) {
    val context = LocalContext.current
    BackHandler {
        navController.navigate(BottomBarScreen.Visualize.route)
    }
    RandomGalleryTopBar(navController, context, visualizeModel)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGalleryTopBar(navController: NavHostController,
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
        ScrollContent(innerPadding, context, navController, visualizeModel)
    }

}

@Composable
fun ScrollContent(innerPadding: PaddingValues,
                  context: Context,
                  navController: NavHostController,
                  visualizeModel: VisualizeModel
) {
    val images by visualizeModel.images.collectAsState()
    visualizeModel.fetchImages()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Display three items per row
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 80.dp // Add padding for BottomNavigationBar height
        )
    ) {
        items(images) { image ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(image.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable {
                        visualizeModel.isFromGallery = true
                        visualizeModel.galleryURI = image.uri
                        visualizeModel.galleryId = image.id

                        val figureKey = image.imageType
                        visualizeModel.selectedOption = Figures.fromKey(figureKey)
                        visualizeModel.latexString = readTexAssets(context,
                            visualizeModel.selectedOption.key)
                        visualizeModel.isRotated = false
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