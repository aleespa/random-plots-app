package com.aleespa.randomsquare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.pages.visualize.BackgroundColorSelector
import com.aleespa.randomsquare.pages.visualize.ColormapDropdown
import com.aleespa.randomsquare.pages.visualize.FractalActions
import com.aleespa.randomsquare.pages.visualize.FractalSettings
import com.aleespa.randomsquare.pages.visualize.GeneratePlotButton
import com.aleespa.randomsquare.pages.visualize.SeedButton

fun LazyListScope.fractalMenu(visualizeModel: VisualizeModel) {
    item {
        val context = LocalContext.current
        FractalActions(visualizeModel, context)
    }
    item { Spacer(Modifier.height(30.dp)) }
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ColormapDropdown(visualizeModel = visualizeModel)
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
    item { FractalSettings(visualizeModel) }
    item { Spacer(Modifier.height(16.dp)) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.newtonMenu(visualizeModel: VisualizeModel) {
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    item { Spacer(Modifier.height(30.dp)) }
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ColormapDropdown(visualizeModel = visualizeModel)
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
    item { FractalSettings(visualizeModel) }
    item { Spacer(Modifier.height(16.dp)) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.standardMenu(visualizeModel: VisualizeModel) {
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    item { Spacer(Modifier.height(30.dp)) }
    item {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            ColormapDropdown(visualizeModel = visualizeModel)
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
    item { BackgroundColorSelector(visualizeModel) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.compositionMenu(visualizeModel: VisualizeModel) {
    item { Spacer(Modifier.height(35.dp)) }
    item {
        val context = LocalContext.current
        GeneratePlotButton(visualizeModel, context)
    }
    item { Spacer(Modifier.height(25.dp)) }
    item { SeedButton(visualizeModel) }
}
