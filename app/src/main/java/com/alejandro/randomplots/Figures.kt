package com.alejandro.randomplots

enum class Figures(val s: String, val s1: String) {

    SPIROGRAPH("Spirograph", "spirograph"),
    CONTINUOUS_SPIROGRAPH("Continuous Spirograph", "cont_spirograph"),
    RANDOM_EIGENVALUES("Random eigenvalues", "random_eigen"),;

    companion object {
        fun fromCode(s: String): Figures? {
            return entries.find { it.s == s }
        }
    }
}