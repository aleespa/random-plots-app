package com.aleespa.randomsquare

import androidx.compose.foundation.lazy.LazyListScope
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.visualize.compositionMenu
import com.aleespa.randomsquare.pages.visualize.fractalMenu
import com.aleespa.randomsquare.pages.visualize.newtonMenu
import com.aleespa.randomsquare.pages.visualize.standardMenu

enum class FigureType(val stringId: Int) {
    FRACTAL(R.string.fractal),
    COMPOSITIONS(R.string.compositions),
    CIRCULAR(R.string.circular),
    POLYGON(R.string.polygon),
    MISC(R.string.misc),
    CHAOS(R.string.chaos),
}

enum class Figures(
    val key: String,
    val figureType: FigureType,
    val resourceStringId: Int,
    val sampleImage: Int,
    val menuType: LazyListScope.(VisualizeModel) -> Unit,
    /** Skia sketch ID: must match the `switch` in cpp/jni_bridge.cpp. Null for non-sketches. */
    val sketchId: Int? = null,
    /** Fractal shader type: must match `u_type` in cpp/fractal_shader.h. Null for non-fractals. */
    val fractalType: Int? = null,
) {

    MANDELBROT(
        "mandelbrot",
        FigureType.FRACTAL,
        R.string.mandelbrot,
        R.drawable.mandelbrot,
        { fractalMenu(it) },
        fractalType = 0
    ),
    JULIA(
        "julia",
        FigureType.FRACTAL,
        R.string.julia,
        R.drawable.julia,
        { fractalMenu(it) },
        fractalType = 1
    ),
    MULTIBROT(
        "multibrot",
        FigureType.FRACTAL,
        R.string.multibrot,
        R.drawable.multibrot,
        { fractalMenu(it) },
        fractalType = 3
    ),
    NEWTON(
        "newton",
        FigureType.FRACTAL,
        R.string.newton,
        R.drawable.newton,
        { newtonMenu(it) },
        fractalType = 4
    ),
    TRICORN(
        "tricorn",
        FigureType.FRACTAL,
        R.string.tricorn,
        R.drawable.tricorn,
        { fractalMenu(it) },
        fractalType = 2
    ),
    SUPER_RANDOM(
        "super",
        FigureType.COMPOSITIONS,
        R.string.super_random,
        R.drawable.super_random,
        { compositionMenu(it) }
    ),
    NOISE(
        "noise",
        FigureType.COMPOSITIONS,
        R.string.noise,
        R.drawable.noise,
        { compositionMenu(it) }
    ),
    BLACKWHITE(
        "bw",
        FigureType.COMPOSITIONS,
        R.string.blackwhite,
        R.drawable.bw,
        { compositionMenu(it) }
    ),
    SOFT(
        "soft",
        FigureType.COMPOSITIONS,
        R.string.soft,
        R.drawable.soft,
        { compositionMenu(it) }
    ),
    CONTINUOUS_SPIROGRAPH(
        "irrational_spirograph",
        FigureType.CIRCULAR,
        R.string.irrational_spirograph,
        R.drawable.irrational_spirograph,
        { standardMenu(it) },
        sketchId = 13
    ),
    ORBITS(
        "orbits",
        FigureType.CHAOS,
        R.string.orbits,
        R.drawable.orbits,
        { standardMenu(it) },
        sketchId = 11
    ),
    SPIRAL(
        "spiral",
        FigureType.CHAOS,
        R.string.spiral,
        R.drawable.spiral,
        { standardMenu(it) },
        sketchId = 5
    ),
    POLYGON_GRID(
        "polygon_grid",
        FigureType.POLYGON,
        R.string.polygon_grid,
        R.drawable.polygon_grid,
        { standardMenu(it) },
        sketchId = 4
    ),
    POLYGON_FEEDBACK(
        "polygon_feedback",
        FigureType.POLYGON,
        R.string.polygon_feedback,
        R.drawable.polygon_feedback,
        { standardMenu(it) },
        sketchId = 8
    ),
    SPIROGRAPH(
        "spirograph",
        FigureType.CIRCULAR,
        R.string.spirograph,
        R.drawable.spirograph,
        { standardMenu(it) },
        sketchId = 12
    ),
    EXPONENTIAL_SUM(
        "exponential_sum",
        FigureType.CIRCULAR,
        R.string.exponential_sum,
        R.drawable.exponential_sum,
        { standardMenu(it) },
        sketchId = 7
    ),
    POLYGON_TUNNEL(
        "polygon_tunnel",
        FigureType.POLYGON,
        R.string.polygon_tunnel,
        R.drawable.polygon_tunnel,
        { standardMenu(it) },
        sketchId = 9
    ),
    NOISY_CIRCLES(
        "noisy_circles",
        FigureType.CIRCULAR,
        R.string.noisy_cicles,
        R.drawable.noisy_circles,
        { standardMenu(it) },
        sketchId = 0
    ),
    WAVES(
        "waves",
        FigureType.MISC,
        R.string.waves,
        R.drawable.waves,
        { standardMenu(it) },
        sketchId = 2
    ),
    CONSTELLATIONS(
        "constellations",
        FigureType.MISC,
        R.string.constellations,
        R.drawable.constellations,
        { standardMenu(it) },
        sketchId = 3
    ),
    ROTATIONS(
        "rotations",
        FigureType.POLYGON,
        R.string.rotations,
        R.drawable.rotations,
        { standardMenu(it) },
        sketchId = 10
    ),
    BUBBLES(
        "bubbles",
        FigureType.MISC,
        R.string.bubbles,
        R.drawable.bubbles,
        { standardMenu(it) },
        sketchId = 1
    ),
    RANDOM_EIGENVALUES(
        "random_eigen",
        FigureType.MISC,
        R.string.random_eigen,
        R.drawable.random_eigen,
        { standardMenu(it) },
        sketchId = 14
    ),
    CUBISM(
        "cubism",
        FigureType.CHAOS,
        R.string.cubism,
        R.drawable.cubism,
        { standardMenu(it) },
        sketchId = 6
    ),
    ;

    companion object {
        fun fromKey(key: String): Figures {
            return entries.find { it.key == key } ?: SPIROGRAPH
        }
    }
}


fun getFiguresByType(figureType: FigureType): List<Figures> {
    return Figures.entries.filter { it.figureType == figureType }
}