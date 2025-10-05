package com.aleespa.randomsquare.pages.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.FilterTypesDialog
import com.aleespa.randomsquare.tools.loadSavedImage
import com.aleespa.randomsquare.tools.parkinsansFontFamily

@Composable
fun Gallery(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    BackHandler {
        navController.navigate(BottomBarScreen.Visualize.route)
    }
    Column(modifier = Modifier.safeDrawingPadding()) {
        RandomGalleryTopBar(navController, context, visualizeModel)
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGalleryTopBar(
    navController: NavHostController,
    context: Context,
    visualizeModel: VisualizeModel
) {

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    modifier = Modifier
                        .height(75.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(), // Optional padding
                            contentAlignment = Alignment.Center // Centers the content inside the Box
                        ) {
                            Text(
                                text = stringResource(id = R.string.saved_images),
                                style = TextStyle(
                                    fontFamily = parkinsansFontFamily,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(), // Optional padding
                            contentAlignment = Alignment.Center // Centers the content inside the Box
                        ) {
                            IconButton(onClick = {
                                navController.navigate(BottomBarScreen.Visualize.route)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Localized description"
                                )
                            }
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(), // Optional padding
                            contentAlignment = Alignment.Center // Centers the content inside the Box
                        ) {
                            IconButton(onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.instagram.com/random_plot")
                                )
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_instagram_logo),
                                    contentDescription = "Open Instagram",
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                    },
                )
                FilterChips(visualizeModel)

            }
        },

        ) { innerPadding ->
        ScrollContent(innerPadding, context, navController, visualizeModel)
    }

}

@Composable
fun FilterChips(visualizeModel: VisualizeModel) {
    LazyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
    ) {
        item {
            FilterChip(
                modifier = Modifier
                    .padding(start = 10.dp),
                onClick = {
                    if (visualizeModel.darkFilter) {
                        visualizeModel.darkFilter = false
                    } else {
                        visualizeModel.darkFilter = true
                        visualizeModel.lightFilter = false
                    }
                },
                label = { Text(stringResource(R.string.dark_name)) },
                selected = visualizeModel.darkFilter,
                leadingIcon = if (visualizeModel.darkFilter) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
            )
        }
        item {
            FilterChip(
                modifier = Modifier
                    .padding(start = 10.dp),
                onClick = {
                    if (visualizeModel.lightFilter) {
                        visualizeModel.lightFilter = false
                    } else {
                        visualizeModel.lightFilter = true
                        visualizeModel.darkFilter = false
                    }
                },
                label = { Text(stringResource(R.string.light_name)) },
                selected = visualizeModel.lightFilter,
                leadingIcon = if (visualizeModel.lightFilter) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
            )
        }
        item {
            FilterChipWithDropdown(visualizeModel)
        }
    }
}


@Composable
fun FilterChipWithDropdown(visualizeModel: VisualizeModel) {
    // The FilterChip
    FilterChip(
        selected = (visualizeModel.filterImageType != "None"), // Show selected state when dialog is visible
        onClick = { visualizeModel.showFilterDialog = true }, // Show dialog on click
        label = {
            if (visualizeModel.filterImageType == "None") {
                Text(text = stringResource(R.string.filter_types))
            } else {
                Text(stringResource(Figures.fromKey(visualizeModel.filterImageType).resourceStringId))
            }
        },
        modifier = Modifier.padding(start = 10.dp),
        trailingIcon = if (visualizeModel.filterImageType == "None") {
            {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
    FilterTypesDialog(visualizeModel)
}

@Composable
fun ScrollContent(
    innerPadding: PaddingValues,
    context: Context,
    navController: NavHostController,
    visualizeModel: VisualizeModel,
) {

    val images by visualizeModel.filteredImages.collectAsState()
    LaunchedEffect(
        visualizeModel.darkFilter,
        visualizeModel.lightFilter,
        visualizeModel.filterImageType
    ) {
        visualizeModel.updateFilteredImages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (images.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.main_icon),
                contentDescription = "Sin imágenes",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
                    .graphicsLayer(alpha = 0.75f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

            )
            Text(
                text = stringResource(R.string.no_images),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
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
                                loadSavedImage(visualizeModel, image, context)
                                navController.navigate(BottomBarScreen.Visualize.route)
                            }
                    )
                }
            }
        }
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