package com.aleespa.randomsquare.pages.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.DeleteAllConfirmationDialog
import com.aleespa.randomsquare.tools.loadSavedImage
import kotlinx.coroutines.launch

@Composable
fun Gallery(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visualizeModel.isFromGallery = false
        visualizeModel.updateFilteredImages()
    }

    BackHandler {
        navController.navigate(BottomBarScreen.Browse.route)
    }

    Column(modifier = Modifier.safeDrawingPadding()) {
        RandomGalleryTopBar(navController, context, visualizeModel)
    }

    if (visualizeModel.showDeleteAllDialog) {
        DeleteAllConfirmationDialog(
            onDismiss = { visualizeModel.showDeleteAllDialog = false },
            onConfirm = {
                visualizeModel.showDeleteAllDialog = false
                scope.launch {
                    visualizeModel.deleteAllImages()
                }
            }
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomGalleryTopBar(
    navController: NavHostController,
    context: Context,
    visualizeModel: VisualizeModel
) {
    var showMenu by remember { mutableStateOf(false) }
    rememberCoroutineScope()

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
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.saved_images),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                navController.navigate(BottomBarScreen.Browse.route)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showMenu = !showMenu },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_filters)) },
                                    onClick = {
                                        visualizeModel.clearFilters()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_all_images)) },
                                    onClick = {
                                        visualizeModel.showDeleteAllDialog = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.instagram)) },
                                    onClick = {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.instagram.com/random_plot")
                                        )
                                        context.startActivity(intent)
                                        showMenu = false
                                    }
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
fun ScrollContent(
    innerPadding: PaddingValues,
    context: Context,
    navController: NavHostController,
    visualizeModel: VisualizeModel,
) {
    val gridState = rememberLazyGridState()

    val images by visualizeModel.filteredImages.collectAsState()

    LaunchedEffect(visualizeModel.scrollToTopGallery) {
        if (visualizeModel.scrollToTopGallery > 0) {
            gridState.animateScrollToItem(0)
        }
    }

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
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
                    .graphicsLayer(alpha = 0.75f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

            )
            Text(
                text = stringResource(R.string.no_images),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        } else {
            LazyVerticalGrid(
                state = gridState,
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
                items(images, key = { it.id }) { image ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
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
