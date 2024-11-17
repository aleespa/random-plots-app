package com.alejandro.randomplots

enum class Figures(
    val key: String,
    val resourceStringId: Int,
    val iconResourceId: Int) {

    SPIROGRAPH(
        "spirograph",
        R.string.spirograph,
        R.drawable.spirograph),
    CONTINUOUS_SPIROGRAPH(
        "cont_spirograph",
        R.string.irrational_spirograph,
        R.drawable.cont_spirograph),
    RANDOM_EIGENVALUES(
        "random_eigen",
        R.string.random_eigen,
        R.drawable.random_eigen);

    companion object {
        fun fromKey(key: String): Figures {
            return entries.find { it.key == key } ?: SPIROGRAPH;
        }
    }
}