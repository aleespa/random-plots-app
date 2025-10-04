package com.aleespa.randomsquare

enum class FigureType(val stringId: Int) {
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
    val sampleDarkImage: Int,
    val sampleLightImage: Int
) {

    SUPER_RANDOM(
        "super",
        FigureType.COMPOSITIONS,
        R.string.super_random,
        R.drawable.super_random_dark,
        R.drawable.super_random_light
    ),
    BLACKWHITE(
        "bw",
        FigureType.COMPOSITIONS,
        R.string.blackwhite,
        R.drawable.bw,
        R.drawable.bw
    ),
    SOFT(
        "soft",
        FigureType.COMPOSITIONS,
        R.string.soft,
        R.drawable.soft_dark,
        R.drawable.soft_light
    ),
    NOISE(
        "noise",
        FigureType.COMPOSITIONS,
        R.string.noise,
        R.drawable.noise,
        R.drawable.noise
    ),
    CONTINUOUS_SPIROGRAPH(
        "irrational_spirograph",
        FigureType.CIRCULAR,
        R.string.irrational_spirograph,
        R.drawable.irrational_spirograph_dark,
        R.drawable.irrational_spiropgrah_light
    ),
    ORBITS(
        "orbits",
        FigureType.CHAOS,
        R.string.orbits,
        R.drawable.orbits_dark,
        R.drawable.orbits_light
    ),
    SPIRAL(
        "spiral",
        FigureType.CHAOS,
        R.string.spiral,
        R.drawable.spiral_dark,
        R.drawable.spiral_light
    ),
    POLYGON_GRID(
        "polygon_grid",
        FigureType.POLYGON,
        R.string.polygon_grid,
        R.drawable.polygon_grid_dark,
        R.drawable.polygon_grid_light
    ),
    POLYGON_FEEDBACK(
        "polygon_feedback",
        FigureType.POLYGON,
        R.string.polygon_feedback,
        R.drawable.polygon_feedback_dark,
        R.drawable.polygon_feedback_light
    ),
    SPIROGRAPH(
        "spirograph",
        FigureType.CIRCULAR,
        R.string.spirograph,
        R.drawable.spirograph_dark,
        R.drawable.spirograph_light
    ),
    EXPONENTIAL_SUM(
        "exponential_sum",
        FigureType.CIRCULAR,
        R.string.exponential_sum,
        R.drawable.exponential_sum_dark,
        R.drawable.exponential_sum_light
    ),
    POLYGON_TUNNEL(
        "polygon_tunnel",
        FigureType.POLYGON,
        R.string.polygon_tunnel,
        R.drawable.polygon_tunnel_dark,
        R.drawable.polygon_tunnel_light
    ),
    NOISY_CIRCLES(
        "noisy_circles",
        FigureType.CIRCULAR,
        R.string.noisy_cicles,
        R.drawable.noisy_circles_dark,
        R.drawable.noisy_circles_light
    ),
    WAVES(
        "waves",
        FigureType.MISC,
        R.string.waves,
        R.drawable.waves_dark,
        R.drawable.waves_light
    ),
    CONSTELLATIONS(
        "constellations",
        FigureType.MISC,
        R.string.constellations,
        R.drawable.constellations_dark,
        R.drawable.constellations_light
    ),
    ROTATIONS(
        "rotations",
        FigureType.POLYGON,
        R.string.rotations,
        R.drawable.rotations_dark,
        R.drawable.rotations_light
    ),
    BUBBLES(
        "bubbles",
        FigureType.MISC,
        R.string.bubbles,
        R.drawable.bubbles_dark,
        R.drawable.bubbles_light
    ),
    RANDOM_EIGENVALUES(
        "random_eigen",
        FigureType.MISC,
        R.string.random_eigen,
        R.drawable.random_eigen_dark,
        R.drawable.random_eigen_light
    ),
    CUBISM(
        "cubism",
        FigureType.CHAOS,
        R.string.cubism,
        R.drawable.cubism_dark,
        R.drawable.cubism_light
    ),
    ;

    companion object {
        fun fromKey(key: String): Figures {
            return entries.find { it.key == key } ?: SPIROGRAPH;
        }
    }
}


fun getFiguresByType(figureType: FigureType): List<Figures> {
    return Figures.entries.filter { it.figureType == figureType }
}