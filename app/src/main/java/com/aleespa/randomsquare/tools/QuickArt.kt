package com.aleespa.randomsquare.tools

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.ImageEntity
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Random figures rendered outside the UI: the art widget, the "New wallpaper" Quick Settings
 * tile and app shortcut. Everything renders on-device with the same engines as Visualize.
 */
object QuickArt {
    /** Outlives the tile/shortcut/widget callback that starts the work (a few hundred ms). */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val wallpaperLock = Mutex()

    /**
     * Fractals that can look different every time: Lyapunov varies with the seed, Julia and
     * Newton get random parameters ([randomView]). The others are defined by their view, so a
     * random one at the default view would always be the same picture.
     */
    private val randomFractals = setOf(Figures.LYAPUNOV, Figures.JULIA, Figures.NEWTON)

    /** Figures of [types] that look different on every render. */
    fun pool(types: Set<FigureType> = FigureType.entries.toSet()): List<Figures> =
        Figures.entries.filter {
            it.figureType in types && (it.figureType != FigureType.FRACTAL || it in randomFractals)
        }

    fun randomFigure(
        types: Set<FigureType> = FigureType.entries.toSet(),
        random: Random = Random,
    ): Figures = pool(types).ifEmpty { pool() }.random(random)

    /** The default view, with random parameters for Julia (c near the Mandelbrot boundary) and Newton. */
    private fun randomView(figure: Figures, random: Random = Random): FractalView {
        val view = FractalView.defaultsFor(figure)
        return when (figure) {
            Figures.JULIA -> {
                // A point on the main cardioid's edge, nudged off it: the interesting Julia sets.
                val t = random.nextDouble(0.0, 2 * Math.PI)
                val r = random.nextDouble(0.985, 1.01)
                val cx = (Math.cos(t) / 2 - Math.cos(2 * t) / 4) * r
                val cy = (Math.sin(t) / 2 - Math.sin(2 * t) / 4) * r
                view.copy(juliaCX = cx, juliaCY = cy)
            }
            Figures.NEWTON -> {
                val coeffs = DoubleArray(9) { (random.nextInt(19) - 9).toDouble() }
                if (coeffs[8] == 0.0) coeffs[8] = 1.0  // keep it degree 8
                view.copy(newtonCoeffs = coeffs)
            }
            else -> view
        }
    }

    /** A colormap that suits [figure]: Prism keeps full colour, others pick any palette but solids. */
    fun randomColormap(figure: Figures, random: Random = Random): Colormaps {
        if (figure.supportsFullColor) return Colormaps.FULL_COLOR
        return Colormaps.entries
            .filter { it.isAvailableFor(figure) && it != Colormaps.SOLID_WHITE && it != Colormaps.SOLID_BLACK }
            .random(random)
    }

    /**
     * Renders a random figure at [width] x [height]. Returns its settings (so the app can reopen
     * it exactly) and the image, or null if the engine failed (e.g. no OpenGL ES 3.1).
     */
    suspend fun render(
        width: Int,
        height: Int,
        figure: Figures = randomFigure(),
    ): Pair<ImageEntity, Bitmap>? =
        withContext(Dispatchers.Default) {
            val colormap = randomColormap(figure)
            val seed = generate32BitSeed()
            val view = randomView(figure)
            val spec = RenderSpec(
                figure = figure, seed = seed, width = width, height = height,
                bgColor = DEFAULT_BG_COLOR, colormap = colormap, fractal = view,
            )
            val bitmap = renderFigure(spec)?.asAndroidBitmap() ?: return@withContext null
            val entity = ImageEntity(
                imageType = figure.key,
                randomSeed = seed,
                backgroundColor = DEFAULT_BG_COLOR,
                isDarkMode = true,
                colormap = colormap.key,
                fractalXCenter = if (figure.hasPanZoomView) view.xCenter else null,
                fractalYCenter = if (figure.hasPanZoomView) view.yCenter else null,
                fractalZoom = if (figure.hasPanZoomView) view.zoom else null,
                iterations = if (figure.figureType == FigureType.FRACTAL) view.iterations else null,
                param1 = when (figure) {
                    Figures.JULIA -> view.juliaCX
                    Figures.NEWTON -> view.newtonCoeffs[0]
                    else -> null
                },
                param2 = when (figure) {
                    Figures.JULIA -> view.juliaCY
                    Figures.NEWTON -> view.newtonCoeffs[1]
                    else -> null
                },
                param3 = if (figure == Figures.NEWTON) view.newtonCoeffs[2] else null,
                param4 = if (figure == Figures.NEWTON) view.newtonCoeffs[3] else null,
                param5 = if (figure == Figures.NEWTON) view.newtonCoeffs[4] else null,
                param6 = if (figure == Figures.NEWTON) view.newtonCoeffs[5] else null,
                param7 = if (figure == Figures.NEWTON) view.newtonCoeffs[6] else null,
                param8 = if (figure == Figures.NEWTON) view.newtonCoeffs[7] else null,
                param9 = if (figure == Figures.NEWTON) view.newtonCoeffs[8] else null,
            )
            entity to bitmap
        }

    /**
     * Renders a random figure at the screen's size and sets it as the home screen wallpaper.
     * Pannable figures render at the screen's aspect ratio; square sketches are centred on their
     * background. Returns false if rendering or setting the wallpaper failed.
     */
    suspend fun setRandomWallpaper(context: Context): Boolean = wallpaperLock.withLock {
        withContext(Dispatchers.Default) {
            val (w, h) = screenSize(context)
            runCatching {
                val types = AppSettingsRepository(context.dataStore).wallpaperFigureTypes.first()
                val figure = randomFigure(types)
                val bitmap = if (figure.hasPanZoomView) {
                    render(w, h, figure)?.second ?: return@runCatching false
                } else {
                    val square = render(w, w, figure)?.second ?: return@runCatching false
                    convertToAspectRatio(square, w, h, Color(DEFAULT_BG_COLOR))
                }
                WallpaperManager.getInstance(context)
                    .setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                true
            }.getOrDefault(false)
        }
    }

    private fun screenSize(context: Context): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
            return bounds.width() to bounds.height()
        }
        val metrics = context.resources.displayMetrics
        return metrics.widthPixels to metrics.heightPixels
    }
}
