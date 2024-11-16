package com.alejandro.randomplots

enum class Figures(val s: String, val s1: String, val iconResourceId: Int) {

    SPIROGRAPH("Spirograph", "spirograph", R.drawable.spirograph),
    CONTINUOUS_SPIROGRAPH("Continuous Spirograph", "cont_spirograph", R.drawable.cont_spirograph),
    RANDOM_EIGENVALUES("Random eigenvalues", "random_eigen", R.drawable.random_eigen),;

    companion object {
        fun fromCode(s: String): Figures? {
            return entries.find { it.s == s }
        }
        fun fromName(s: String): Figures? {
            return entries.find { it.s1 == s }
        }
    }
}