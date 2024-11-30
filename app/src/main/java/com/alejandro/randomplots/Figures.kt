package com.alejandro.randomplots

enum class Figures(
    val key: String,
    val resourceStringId: Int,
    val iconResourceId: Int) {

    POLYGON_FEEDBACK(
    "polygon_feedback",
    R.string.polygon_feedback,
    R.drawable.polygon_feedback),
    SPIROGRAPH(
        "spirograph",
        R.string.spirograph,
        R.drawable.spirograph),
    CONTINUOUS_SPIROGRAPH(
        "irrational_spirograph",
        R.string.irrational_spirograph,
        R.drawable.irrational_spirograph),
    RANDOM_EIGENVALUES(
        "random_eigen",
        R.string.random_eigen,
        R.drawable.random_eigen),
    POLYGON_TUNNEL(
    "polygon_tunnel",
    R.string.polygon_tunnel,
    R.drawable.polygon_tunnel);

    companion object {
        fun fromKey(key: String): Figures {
            return entries.find { it.key == key } ?: SPIROGRAPH;
        }
    }
}