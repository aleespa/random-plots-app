package com.aleespa.randomsquare.pages.browse

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.getFiguresByType
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.readTexAssets

@Composable
fun BrowserScrollable(
    visualizeModel: VisualizeModel,
    innerPadding: PaddingValues,
    navController: NavHostController,
    context: Context
) {
    val listState = rememberLazyListState()

    LaunchedEffect(visualizeModel.scrollToTopBrowse) {
        if (visualizeModel.scrollToTopBrowse > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding() + 80.dp
        )
    ) {
        item {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() - 10.dp))
        }
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
        val carouselState = rememberCarouselState { figures.count() }

        PlotTypeDescription(stringResource(figureType.stringId))
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier
                .width(412.dp)
                .height(221.dp),
            preferredItemWidth = 186.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(
                state = carouselState,
                snapAnimationSpec = spring(
                    stiffness = 5000f,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
        ) { i ->
            val item = figures[i]
            val sampleImage = item.sampleImage
            val painter = rememberAsyncImagePainter(
                model = sampleImage,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.High
            )
            Box(
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
                    }
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = stringResource(item.resourceStringId),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.5f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.7f)
                            )
                        )
                )
                Text(
                    text = stringResource(item.resourceStringId),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

        }
    }
}


@Composable
fun PlotTypeDescription(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
