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
    val menuType: LazyListScope.(VisualizeModel) -> Unit
) {

    MANDELBROT(
        "mandelbrot",
        FigureType.FRACTAL,
        R.string.mandelbrot,
        R.drawable.mandelbrot,
        { fractalMenu(it) }
    ),
    JULIA(
        "julia",
        FigureType.FRACTAL,
        R.string.julia,
        R.drawable.julia,
        { fractalMenu(it) }
    ),
    MULTIBROT(
        "multibrot",
        FigureType.FRACTAL,
        R.string.multibrot,
        R.drawable.multibrot,
        { fractalMenu(it) }
    ),
    NEWTON(
        "newton",
        FigureType.FRACTAL,
        R.string.newton,
        R.drawable.newton,
        { newtonMenu(it) }
    ),
    TRICORN(
        "tricorn",
        FigureType.FRACTAL,
        R.string.tricorn,
        R.drawable.tricorn,
        { fractalMenu(it) }
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
        { standardMenu(it) }
    ),
    ORBITS(
        "orbits",
        FigureType.CHAOS,
        R.string.orbits,
        R.drawable.orbits,
        { standardMenu(it) }
    ),
    SPIRAL(
        "spiral",
        FigureType.CHAOS,
        R.string.spiral,
        R.drawable.spiral,
        { standardMenu(it) }
    ),
    POLYGON_GRID(
        "polygon_grid",
        FigureType.POLYGON,
        R.string.polygon_grid,
        R.drawable.polygon_grid,
        { standardMenu(it) }
    ),
    POLYGON_FEEDBACK(
        "polygon_feedback",
        FigureType.POLYGON,
        R.string.polygon_feedback,
        R.drawable.polygon_feedback,
        { standardMenu(it) }
    ),
    SPIROGRAPH(
        "spirograph",
        FigureType.CIRCULAR,
        R.string.spirograph,
        R.drawable.spirograph,
        { standardMenu(it) }
    ),
    EXPONENTIAL_SUM(
        "exponential_sum",
        FigureType.CIRCULAR,
        R.string.exponential_sum,
        R.drawable.exponential_sum,
        { standardMenu(it) }
    ),
    POLYGON_TUNNEL(
        "polygon_tunnel",
        FigureType.POLYGON,
        R.string.polygon_tunnel,
        R.drawable.polygon_tunnel,
        { standardMenu(it) }
    ),
    NOISY_CIRCLES(
        "noisy_circles",
        FigureType.CIRCULAR,
        R.string.noisy_cicles,
        R.drawable.noisy_circles,
        { standardMenu(it) }
    ),
    WAVES(
        "waves",
        FigureType.MISC,
        R.string.waves,
        R.drawable.waves,
        { standardMenu(it) }
    ),
    CONSTELLATIONS(
        "constellations",
        FigureType.MISC,
        R.string.constellations,
        R.drawable.constellations,
        { standardMenu(it) }
    ),
    ROTATIONS(
        "rotations",
        FigureType.POLYGON,
        R.string.rotations,
        R.drawable.rotations,
        { standardMenu(it) }
    ),
    BUBBLES(
        "bubbles",
        FigureType.MISC,
        R.string.bubbles,
        R.drawable.bubbles,
        { standardMenu(it) }
    ),
    RANDOM_EIGENVALUES(
        "random_eigen",
        FigureType.MISC,
        R.string.random_eigen,
        R.drawable.random_eigen,
        { standardMenu(it) }
    ),
    CUBISM(
        "cubism",
        FigureType.CHAOS,
        R.string.cubism,
        R.drawable.cubism,
        { standardMenu(it) }
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