package com.aleespa.randomsquare.pages.browse

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
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
import coil3.compose.rememberAsyncImagePainter
import com.aleespa.randomsquare.BottomBarScreen
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.SharedImageKeys
import com.aleespa.randomsquare.pages.sharedImage
import com.aleespa.randomsquare.getFiguresByType
import com.aleespa.randomsquare.tools.LastRenders
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.openFreshFigure
import com.aleespa.randomsquare.tools.reopenLastRender
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import com.aleespa.randomsquare.tools.readTexAssets

/** Horizontal gutter shared by the section headings, carousels and grids. */
private val SectionPadding = 16.dp
private val CardSpacing = 8.dp
private val CardHeight = 205.dp

/** Windows at least this wide show each section as a grid instead of a carousel. */
private val GridLayoutMinWidth = 600.dp
private val GridMinCardWidth = 200.dp

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
            bottom = innerPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() - 10.dp))
        }
        item { FigureSections(visualizeModel, navController, context) }
    }
}

/**
 * One titled section per [FigureType]. Compact windows (phones) use a horizontal carousel; wider
 * windows lay the cards out in a grid that fills the width, since every section would otherwise
 * fit in a short row and leave the rest of the screen empty.
 */
@Composable
fun FigureSections(
    visualizeModel: VisualizeModel, navController: NavHostController,
    context: Context = LocalContext.current
) {
    val scope = rememberCoroutineScope()
    val onFigureClick: (Figures) -> Unit = { figure ->
        scope.launch {
            // Reopen the figure's last render (what its card shows); the first time, generate.
            // The preview loads off the main thread before navigating, so the card grows into
            // the same picture.
            if (!reopenLastRender(visualizeModel, figure, context)) {
                openFreshFigure(visualizeModel, figure, context)
            }
            navController.navigate(BottomBarScreen.Visualize.route)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useGrid = maxWidth >= GridLayoutMinWidth
        val gridColumns = (((maxWidth - SectionPadding * 2 + CardSpacing) /
                (GridMinCardWidth + CardSpacing)).toInt()).coerceAtLeast(2)

        Column {
            for (figureType in FigureType.entries) {
                val figures = getFiguresByType(figureType)
                PlotTypeDescription(stringResource(figureType.stringId))
                if (useGrid) {
                    FigureGrid(figures, gridColumns, onFigureClick)
                } else {
                    FigureCarousel(figures, onFigureClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FigureCarousel(figures: List<Figures>, onFigureClick: (Figures) -> Unit) {
    val carouselState = rememberCarouselState { figures.count() }
    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .height(221.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = CardSpacing,
        contentPadding = PaddingValues(horizontal = SectionPadding),
        flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(
            state = carouselState,
            snapAnimationSpec = spring(
                stiffness = 5000f,
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        )
    ) { i ->
        val figure = figures[i]
        FigureCard(
            figure = figure,
            modifier = Modifier
                .height(CardHeight)
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable { onFigureClick(figure) }
        )
    }
}

@Composable
private fun FigureGrid(figures: List<Figures>, columns: Int, onFigureClick: (Figures) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SectionPadding, vertical = CardSpacing),
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        figures.chunked(columns).forEach { rowFigures ->
            Row(horizontalArrangement = Arrangement.spacedBy(CardSpacing)) {
                rowFigures.forEach { figure ->
                    FigureCard(
                        figure = figure,
                        modifier = Modifier
                            .weight(1f)
                            .height(CardHeight)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable { onFigureClick(figure) }
                    )
                }
                // Keep cards in a partial last row the same width as the others.
                repeat(columns - rowFigures.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Sample image with a bottom scrim and the figure name; shared by carousel and grid. */
@Composable
private fun FigureCard(figure: Figures, modifier: Modifier) {
    val context = LocalContext.current
    // The figure's last render if it has one (keyed by its timestamp so a new one shows at
    // once), otherwise the bundled sample.
    val stamp = LastRenders.stamps[figure.key]
    val model: Any = remember(stamp) {
        LastRenders.previewFile(context, figure)?.let { file ->
            ImageRequest.Builder(context)
                .data(file)
                .memoryCacheKey("last-${figure.key}-$stamp")
                .build()
        } ?: figure.sampleImage
    }
    val painter = rememberAsyncImagePainter(
        model = model,
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.High
    )
    Box(
        modifier = modifier.sharedImage(
            SharedImageKeys.figure(figure), MaterialTheme.shapes.extraLarge
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = stringResource(figure.resourceStringId),
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
            text = stringResource(figure.resourceStringId),
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


@Composable
fun PlotTypeDescription(text: String) {
    // Start-aligned with the 16.dp section padding, so headings line up with the first card.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = SectionPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
