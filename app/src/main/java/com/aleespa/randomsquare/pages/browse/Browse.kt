package com.aleespa.randomsquare.pages.browse

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.getFiguresByType
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.parkinsansFontFamily
import com.aleespa.randomsquare.tools.readTexAssets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Browse(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {

    val context = LocalContext.current

    BackHandler {
        (context as? Activity)?.finish()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {},
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (visualizeModel.settingDarkMode) {
                                SettingDarkMode.On -> ""
                                SettingDarkMode.Off -> ""
                                SettingDarkMode.Auto -> stringResource(id = R.string.auto)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(onClick = {
                            visualizeModel.settingDarkMode = when (visualizeModel.settingDarkMode) {
                                SettingDarkMode.Auto -> SettingDarkMode.On
                                SettingDarkMode.On -> SettingDarkMode.Off
                                SettingDarkMode.Off -> SettingDarkMode.Auto
                            }
                            visualizeModel.bgColor = 0
                        }) {
                            Icon(
                                imageVector = when (visualizeModel.settingDarkMode) {
                                    SettingDarkMode.Auto -> Icons.Default.DarkMode
                                    SettingDarkMode.On -> Icons.Default.DarkMode
                                    SettingDarkMode.Off -> Icons.Default.LightMode
                                },
                                contentDescription = "Change dark mode",
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // Place your scrolling content here
        BrowserScrollable(
            visualizeModel,
            innerPadding,
            navController, context
        )
    }
}


@Composable
fun BrowserScrollable(
    visualizeModel: VisualizeModel,
    innerPadding: PaddingValues,
    navController: NavHostController,
    context: Context
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            top = innerPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + 80.dp
        )
    ) {
        item { Carousel(visualizeModel, navController, context) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Carousel(
    visualizeModel: VisualizeModel, navController: NavHostController,
    context: Context = LocalContext.current
) {

    for (figureType in FigureType.entries) {
        val figures = getFiguresByType(figureType)

        PlotTypeDescription(stringResource(figureType.stringId))
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { figures.count() },
            modifier = Modifier
                .width(412.dp)
                .height(221.dp),
            preferredItemWidth = 186.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(
                state = rememberCarouselState { figures.count() },
                snapAnimationSpec = spring(stiffness = Spring.StiffnessHigh)
            )
        ) { i ->
            val item = figures[i]
            val isDark = isSystemInDarkTheme()
            val sampleImage = when (visualizeModel.settingDarkMode) {
                SettingDarkMode.Auto -> if (isDark) item.sampleDarkImage else (item.sampleLightImage)
                SettingDarkMode.On -> item.sampleDarkImage
                SettingDarkMode.Off -> item.sampleLightImage
            }
            val painter = rememberAsyncImagePainter(
                model = sampleImage,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.High
            )
            Image(
                modifier = Modifier
                    .height(205.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        visualizeModel.selectedFigure = item

                        visualizeModel.latexString = readTexAssets(
                            context,
                            visualizeModel.selectedFigure.key
                        )
                        generateNewPlot(visualizeModel, context)
                        navController.navigate(BottomBarScreen.Visualize.route)
                    },
                painter = painter,
                contentDescription = stringResource(item.resourceStringId),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
fun PlotTypeDescription(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp), // Optional padding
        contentAlignment = Alignment.Center // Centers the content inside the Box
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = parkinsansFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
