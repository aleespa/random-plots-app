package com.aleespa.randomsquare

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Localized names for the descriptive colormaps. Keyed by [Colormaps.text] because several entries
 * share a name (e.g. a regular and a fractal-specific "Copper"). Scientific colormap names
 * (Viridis, Magma, Inferno, Plasma, ...) are not translated and fall back to [Colormaps.text].
 */
@StringRes
private fun Colormaps.nameRes(): Int? = when (text) {
    "Full Color" -> R.string.cm_full_color
    "Solid White" -> R.string.cm_solid_white
    "Solid Black" -> R.string.cm_solid_black
    "Rainbow" -> R.string.cm_rainbow
    "Heat" -> R.string.cm_heat
    "Cool" -> R.string.cm_cool
    "Grayscale" -> R.string.cm_grayscale
    "Ocean" -> R.string.cm_ocean
    "Forest" -> R.string.cm_forest
    "Sunset" -> R.string.cm_sunset
    "Pastel" -> R.string.cm_pastel
    "Copper" -> R.string.cm_copper
    "Bone" -> R.string.cm_bone
    "Spring" -> R.string.cm_spring
    "Summer" -> R.string.cm_summer
    "Autumn" -> R.string.cm_autumn
    "Winter" -> R.string.cm_winter
    "Hot" -> R.string.cm_hot
    "White" -> R.string.cm_white
    "Black" -> R.string.cm_black
    "Red" -> R.string.cm_red
    "Ice" -> R.string.cm_ice
    "Blue" -> R.string.cm_blue
    "Emerald" -> R.string.cm_emerald
    "Fire" -> R.string.cm_fire
    "Electric" -> R.string.cm_electric
    else -> null
}

/** Name to show in the UI for this colormap, in the current language. */
@Composable
fun Colormaps.displayName(): String = nameRes()?.let { stringResource(it) } ?: text
