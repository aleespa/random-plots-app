package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.AspectRatioDialog
import com.aleespa.randomsquare.pages.ColormapSelectionDialog
import com.aleespa.randomsquare.tools.generateNewtonLatex
import com.aleespa.randomsquare.tools.loadBitmapFromFile
import com.aleespa.randomsquare.tools.readTexAssets
import com.aleespa.randomsquare.ui.theme.Spacing

/** Single-column content never gets wider than this (keeps the square image sane on tablets). */
private val MaxSingleColumnWidth = 600.dp

/** Controls column width cap in the two-pane layout. */
private val MaxControlsPaneWidth = 420.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Visualize(
    visualizeModel: VisualizeModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    // Use LaunchedEffect to load cache only once when needed,
    // and only if we are NOT viewing an image from the gallery.
    LaunchedEffect(visualizeModel.isFromGallery, visualizeModel.selectedFigure) {
        // Not while a render is on its way (a figure opened from Browse clears the image).
        if (!visualizeModel.isFromGallery && visualizeModel.imageBitmapState == null &&
            !visualizeModel.loadingPlotGenerator
        ) {
            val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
            if (savedBitmap != null) {
                visualizeModel.imageBitmapState = savedBitmap.asImageBitmap()
                if (visualizeModel.latexString.isEmpty()) {
                    visualizeModel.latexString =
                        readTexAssets(context, visualizeModel.selectedFigure.key)
                }
                if (visualizeModel.selectedFigure == Figures.NEWTON && visualizeModel.newtonLatexString.isEmpty()) {
                    visualizeModel.newtonLatexString =
                        generateNewtonLatex(visualizeModel.newtonCoeffs)
                }
            }
        }
    }
    if (visualizeModel.showAspectRatioDialog) {
        AspectRatioDialog(
            visualizeModel,
            onDismiss = { visualizeModel.showAspectRatioDialog = false },
            onConfirm = {
                visualizeModel.showAspectRatioDialog = false
                setWallpaperAfterAd(visualizeModel, context)
            }
        )
    }

    if (visualizeModel.showColormapDialog) {
        ColormapSelectionDialog(
            selectedColormap = visualizeModel.selectedColormap,
            figure = visualizeModel.selectedFigure,
            onColormapChange = {
                visualizeModel.selectedColormap = it
                com.aleespa.randomsquare.tools.generateNewPlot(
                    visualizeModel,
                    context,
                    randomizeSeed = false,
                    showAds = false
                )
            },
            onDismiss = { visualizeModel.showColormapDialog = false }
        )
    }

    val isDark = when (visualizeModel.settingDarkMode) {
        SettingDarkMode.Auto -> isSystemInDarkTheme()
        SettingDarkMode.On -> true
        SettingDarkMode.Off -> false
    }

    // The backdrop is a tiny pre-blurred copy of the image, stretched with bilinear filtering:
    // it looks like a large blur but costs nothing per frame (a live 40 dp blur of the full image
    // was redrawn on every frame of the screen transitions).
    val backdrop by produceState<ImageBitmap?>(null, visualizeModel.imageBitmapState) {
        val source = visualizeModel.imageBitmapState
        value = source?.let { withContext(Dispatchers.Default) { blurredThumbnail(it.asAndroidBitmap()) } }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        backdrop?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            color = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(
                                alpha = 0.6f
                            ),
                            blendMode = BlendMode.SrcOver
                        )
                    }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val useTwoPanes = maxWidth >= 600.dp && maxWidth > maxHeight
            if (useTwoPanes) {
                TwoPaneVisualize(visualizeModel)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxHeight()
                        .widthIn(max = MaxSingleColumnWidth),
                ) {
                    item { HeaderSection(visualizeModel, context) }
                    item { Spacer(Modifier.height(Spacing.s)) }
                    item { VisualizeBox(visualizeModel) }
                    item { SeedText(visualizeModel) }

                    // Menus own their spacing (see Menus.kt), including the bottom gap.
                    with(visualizeModel.selectedFigure) {
                        menuType(visualizeModel)
                    }
                }
            }
        }
    }
}

/**
 * Landscape / wide layout: the image takes the full available height on the left, the header
 * and controls scroll in a column on the right.
 */
@Composable
private fun TwoPaneVisualize(visualizeModel: VisualizeModel) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VisualizeBox(
                    visualizeModel,
                    modifier = Modifier.aspectRatio(1f, matchHeightConstraintsFirst = true)
                )
            }
            SeedText(visualizeModel)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = MaxControlsPaneWidth)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item { HeaderSection(visualizeModel, context) }

            with(visualizeModel.selectedFigure) {
                menuType(visualizeModel)
            }
        }
    }
}

/** Side of the backdrop thumbnail's longer edge, in pixels. */
private const val BackdropSize = 48

/**
 * A [BackdropSize]-pixel copy of [source] with a three-pass box blur (close to a Gaussian),
 * meant to be stretched over the whole screen.
 */
internal fun blurredThumbnail(source: android.graphics.Bitmap): ImageBitmap {
    val scale = BackdropSize.toFloat() / maxOf(source.width, source.height)
    val w = (source.width * scale).toInt().coerceAtLeast(1)
    val h = (source.height * scale).toInt().coerceAtLeast(1)
    // Two steps, so the downscale averages pixels instead of sampling a sparse grid.
    val mid = android.graphics.Bitmap.createScaledBitmap(source, w * 4, h * 4, true)
    val small = android.graphics.Bitmap.createScaledBitmap(mid, w, h, true)
    val px = IntArray(w * h)
    small.getPixels(px, 0, w, 0, 0, w, h)
    val tmp = IntArray(w * h)
    repeat(3) {
        boxBlur(px, tmp, w, h, horizontal = true)
        boxBlur(tmp, px, w, h, horizontal = false)
    }
    val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
    out.setPixels(px, 0, w, 0, 0, w, h)
    return out.asImageBitmap()
}

private fun boxBlur(src: IntArray, dst: IntArray, w: Int, h: Int, horizontal: Boolean, r: Int = 2) {
    val lines = if (horizontal) h else w
    val len = if (horizontal) w else h
    for (line in 0 until lines) {
        for (i in 0 until len) {
            var a = 0; var red = 0; var g = 0; var b = 0; var n = 0
            for (k in (i - r).coerceAtLeast(0)..(i + r).coerceAtMost(len - 1)) {
                val c = if (horizontal) src[line * w + k] else src[k * w + line]
                a += c ushr 24; red += (c shr 16) and 0xFF; g += (c shr 8) and 0xFF; b += c and 0xFF; n++
            }
            val idx = if (horizontal) line * w + i else i * w + line
            dst[idx] = ((a / n) shl 24) or ((red / n) shl 16) or ((g / n) shl 8) or (b / n)
        }
    }
}
