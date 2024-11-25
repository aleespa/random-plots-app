package com.alejandro.randomplots.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.alejandro.randomplots.tools.loadSavedImage


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
            Column {
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
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/random_plot"))
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_instagram_logo),
                                contentDescription = "Open Instagram",
                                modifier = Modifier.size(35.dp)
                            )
                        }

                    },
                    scrollBehavior = scrollBehavior
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
            .padding(vertical = 10.dp)
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
                    visualizeModel.lightFilter = false}
                },
                label = { Text("Dark") },
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
                label = { Text("Light") },
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
    val options = Figures.entries.map { it }
    // The FilterChip
    FilterChip(
        selected = (visualizeModel.filterImageType != "None" ), // Show selected state when dialog is visible
        onClick = { visualizeModel.showFilterDialog = true }, // Show dialog on click
        label = { if (visualizeModel.filterImageType == "None" ) {
            Text("Figure type")
        } else {Text(stringResource(Figures.fromKey(visualizeModel.filterImageType).resourceStringId))}},
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

    if (visualizeModel.showFilterDialog) {
        AlertDialog(
            onDismissRequest = { visualizeModel.showFilterDialog = false }, // Close the dialog when clicked outside
            title = { Text("Figure type") },
            text = {
                Column {
                    options.forEach { option ->
                        TextButton(
                            onClick = {
                                visualizeModel.filterImageType = option.key // Update the selected option
                                visualizeModel.showFilterDialog = false // Close the dialog
                            }
                        ) {
                            Text(
                                text = stringResource(option.resourceStringId),
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            visualizeModel.filterImageType = "None" // Update the selected option
                            visualizeModel.showFilterDialog = false // Close the dialog
                        }
                    ) {
                        Text(
                            text = "All figures",
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { visualizeModel.showFilterDialog = false } // Close dialog without making a selection
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun ScrollContent(innerPadding: PaddingValues,
                  context: Context,
                  navController: NavHostController,
                  visualizeModel: VisualizeModel,
) {

    val images by visualizeModel.filteredImages.collectAsState()
    LaunchedEffect(visualizeModel.darkFilter, visualizeModel.lightFilter, visualizeModel.filterImageType) {
        visualizeModel.updateFilteredImages()
    }


    Column {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Display three items per row
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) ,
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
                            loadSavedImage(visualizeModel, image, context)
                            navController.navigate(BottomBarScreen.Visualize.route)
                        }
                )
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