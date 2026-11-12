package com.aleespa.randomsquare.ui.theme

import androidx.compose.ui.unit.dp

/** Spacing scale used across screens. Prefer these over ad-hoc dp values. */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp

    /** Horizontal margin between screen content and the window edge. */
    val screenGutter = 16.dp
}

/** Shared component sizes. */
object Sizes {
    /** Height of the primary action button groups on the Visualize screen. */
    val actionButtonHeight = 56.dp

    /** Max width of centered control columns (button groups, colormap picker, seed field). */
    val maxControlWidth = 300.dp

    /** Max width of form-like content (Settings) and wide dialogs. */
    val maxContentWidth = 560.dp
}
