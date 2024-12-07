package com.aleespa.randomsquare

enum class Figures(
    val key: String,
    val figureType: FigureType,
    val resourceStringId: Int,
    val iconResourceId: Int,
    val sampleDarkImage: Int,
    val sampleLightImage: Int) {

    POLYGON_FEEDBACK(
        "polygon_feedback",
        FigureType.POLYGON,
        R.string.polygon_feedback,
        R.drawable.polygon_feedback,
        R.drawable.polygon_feedback_dark,
        R.drawable.polygon_feedback_light),
    SPIROGRAPH(
        "spirograph",
        FigureType.CIRCULAR,
        R.string.spirograph,
        R.drawable.spirograph,
        R.drawable.spirograph_dark,
        R.drawable.spirograph_light),
    CONTINUOUS_SPIROGRAPH(
        "irrational_spirograph",
        FigureType.CIRCULAR,
        R.string.irrational_spirograph,
        R.drawable.irrational_spirograph,
        R.drawable.irrational_spirograph_dark,
        R.drawable.irrational_spiropgrah_light),
    RANDOM_EIGENVALUES(
        "random_eigen",
        FigureType.STOCHASTIC,
        R.string.random_eigen,
        R.drawable.random_eigen,
        R.drawable.random_eigen_dark,
        R.drawable.random_eigen_light),
    POLYGON_TUNNEL(
        "polygon_tunnel",
        FigureType.POLYGON,
        R.string.polygon_tunnel,
        R.drawable.polygon_tunnel,
        R.drawable.polygon_tunnel_dark,
        R.drawable.polygon_tunnel_light);

    companion object {
        fun fromKey(key: String): Figures {
            return entries.find { it.key == key } ?: SPIROGRAPH;
        }
    }
}

enum class FigureType(val stringId: Int){
    CIRCULAR(R.string.circular),
    POLYGON(R.string.polygon),
    STOCHASTIC(R.string.stochastic)
}

fun getFiguresByType(figureType: FigureType): List<Figures> {
    return Figures.entries.filter { it.figureType == figureType }
}