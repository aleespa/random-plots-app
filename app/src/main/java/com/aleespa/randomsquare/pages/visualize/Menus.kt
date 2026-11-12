package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.ui.theme.Spacing

// Every menu uses the same rhythm: a small gap above the action buttons, then Spacing.l between
// sections, so switching figure types doesn't make the controls jump around.

private fun LazyListScope.smallGap() = item { Spacer(Modifier.height(Spacing.s)) }
private fun LazyListScope.sectionGap() = item { Spacer(Modifier.height(Spacing.l)) }

private fun LazyListScope.colormapPicker(visualizeModel: VisualizeModel) = item {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ColormapDropdown(visualizeModel = visualizeModel)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.fractalMenu(visualizeModel: VisualizeModel) {
    smallGap()
    item {
        val context = LocalContext.current
        FractalActions(visualizeModel, context)
    }
    sectionGap()
    colormapPicker(visualizeModel)
    sectionGap()
    item { FractalSettings(visualizeModel) }
    sectionGap()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.newtonMenu(visualizeModel: VisualizeModel) {
    smallGap()
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    sectionGap()
    colormapPicker(visualizeModel)
    sectionGap()
    item { FractalSettings(visualizeModel) }
    sectionGap()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.standardMenu(visualizeModel: VisualizeModel) {
    smallGap()
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    sectionGap()
    colormapPicker(visualizeModel)
    sectionGap()
    item { BackgroundColorSelector(visualizeModel) }
    sectionGap()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.compositionMenu(visualizeModel: VisualizeModel) {
    smallGap()
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    sectionGap()
    item { SeedButton(visualizeModel) }
    sectionGap()
}
