package com.alejandro.randomplots

enum class Figures(val fullName: String, val scriptName: String, val iconResourceId: Int) {

    SPIROGRAPH("Spirograph", "spirograph", R.drawable.spirograph),
    CONTINUOUS_SPIROGRAPH("Continuous Spirograph", "cont_spirograph", R.drawable.cont_spirograph),
    RANDOM_EIGENVALUES("Random eigenvalues", "random_eigen", R.drawable.random_eigen),;

    companion object {
        fun fromCode(s: String): Figures? {
            return entries.find { it.fullName == s }
        }
        fun fromName(s: String): Figures? {
            return entries.find { it.scriptName == s }
        }
    }
}