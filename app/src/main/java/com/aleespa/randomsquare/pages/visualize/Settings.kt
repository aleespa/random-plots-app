package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.hslToColor
import com.aleespa.randomsquare.tools.toHsl
import kotlin.math.roundToInt

@Composable
fun BackgroundColorSelector(
    bgColor: Int,
    onColorChange: (Int) -> Unit
) {
    val initialHsl = remember(bgColor) {
        Color(bgColor).toHsl()
    }

    var hue by remember { mutableFloatStateOf(initialHsl[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsl[1]) }
    var lightness by remember { mutableFloatStateOf(initialHsl[2]) }

    val currentColor = hslToColor(hue, saturation, lightness)

    LaunchedEffect(hue, saturation, lightness) {
        onColorChange(currentColor.toArgb())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text("Hue: ${hue.roundToInt()}")
        val hueGradientColors = (0..6).map { i ->
            val hueValue = i * 60f
            hslToColor(hueValue, saturation, lightness)
        } + listOf(hslToColor(0f, saturation, lightness))

        GradientSlider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            gradient = Brush.horizontalGradient(hueGradientColors),
            thumbColor = currentColor
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Text("Saturation: ${(saturation * 100).roundToInt()}%")
                    GradientSlider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                        gradient = Brush.horizontalGradient(
                            listOf(
                                hslToColor(hue, 0f, lightness),
                                hslToColor(hue, 1f, lightness)
                            )
                        ),
                        thumbColor = currentColor
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Text("Lightness: ${(lightness * 100).roundToInt()}%")
                    GradientSlider(
                        value = lightness,
                        onValueChange = { lightness = it },
                        valueRange = 0f..1f,
                        gradient = Brush.horizontalGradient(
                            listOf(Color.Black, hslToColor(hue, saturation, 0.5f), Color.White)
                        ),
                        thumbColor = currentColor
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundColorSelector(visualizeModel: VisualizeModel) {
    BackgroundColorSelector(
        bgColor = visualizeModel.bgColor,
        onColorChange = { visualizeModel.bgColor = it }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    gradient: Brush,
    thumbColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .background(brush = gradient, shape = MaterialTheme.shapes.small),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                thumbColor = Color.Transparent
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(32.dp)
                        .shadow(4.dp, RoundedCornerShape(6.dp))
                        .background(thumbColor, RoundedCornerShape(6.dp))
                        .border(
                            3.dp,
                            Color.White.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                )
            }
        )
    }
}

@Composable
fun ColormapDropdown(
    selectedColormap: Colormaps,
    isFractal: Boolean,
    onColormapChange: (Colormaps) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .widthIn(max = 200.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedColormap.key,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ColormapSineWaveLine(
                colormap = selectedColormap,
                modifier = Modifier.size(width = 80.dp, height = 20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            val filteredColormaps = if (isFractal) {
                Colormaps.entries.filter { it.isFractalSpecific || it == Colormaps.RAINBOW || it == Colormaps.GRAYSCALE }
            } else {
                Colormaps.entries.filter { !it.isFractalSpecific }
            }

            filteredColormaps.forEach { colormap ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(colormap.key, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(35.dp))
                            ColormapSineWaveLine(
                                colormap = colormap,
                                modifier = Modifier.size(width = 80.dp, height = 18.dp)
                            )
                        }
                    },
                    onClick = {
                        onColormapChange(colormap)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColormapDropdown(
    visualizeModel: VisualizeModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isFractal = visualizeModel.selectedFigure.figureType == com.aleespa.randomsquare.FigureType.FRACTAL
    ColormapDropdown(
        selectedColormap = visualizeModel.selectedColormap,
        isFractal = isFractal,
        onColormapChange = { 
            visualizeModel.selectedColormap = it
            if (isFractal) {
                generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
            }
        },
        modifier = modifier
    )
}

@Composable
fun FractalSettings(visualizeModel: VisualizeModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iterations: ${visualizeModel.fractalIterations}")
        Slider(
            value = visualizeModel.fractalIterations.toFloat(),
            onValueChange = { 
                visualizeModel.fractalIterations = it.toInt()
                generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
            },
            valueRange = 50f..2000f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(35.dp)
        )

        if (visualizeModel.selectedFigure == Figures.JULIA) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "cx: ${String.format("%.3f", visualizeModel.juliaCX)}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = visualizeModel.juliaCX.toFloat(),
                        onValueChange = { 
                            visualizeModel.juliaCX = it.toDouble()
                            generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
                        },
                        valueRange = -2f..2f,
                        modifier = Modifier.height(30.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "cy: ${String.format("%.3f", visualizeModel.juliaCY)}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = visualizeModel.juliaCY.toFloat(),
                        onValueChange = { 
                            visualizeModel.juliaCY = it.toDouble()
                            generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
                        },
                        valueRange = -2f..2f,
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ColormapSineWaveLine(
    colormap: Colormaps,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 18f
) {
    Canvas(
        modifier = modifier
            .height(40.dp)
            .fillMaxWidth()
    ) {
        val width = size.width
        val height = size.height
        val amplitude = height / 4f
        val frequency = 4 * Math.PI / width

        val path = Path().apply {
            moveTo(0f, height / 2f)
            for (x in 0..width.toInt()) {
                val y = (height / 2f + amplitude * kotlin.math.sin(frequency * x)).toFloat()
                lineTo(x.toFloat(), y)
            }
        }

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(colormap.colorlist.map { Color(it) }),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun SeedButton(visualizeModel: VisualizeModel) {
    var text by remember { mutableStateOf(visualizeModel.randomSeed.toString()) }

    OutlinedTextField(
        value = if (visualizeModel.userSeed.not()) "" else visualizeModel.randomSeed.toString(),
        onValueChange = { input ->
            if (input.all { it.isDigit() } && input.length <= 19) {
                text = input
                if (input.isNotEmpty()) {
                    visualizeModel.userSeed = true
                    visualizeModel.randomSeed = input.toLong()
                }
            } else if (input.isEmpty()) {
                visualizeModel.userSeed = false
            }
        },
        modifier = Modifier.padding(horizontal = 95.dp),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text("Seed") },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    visualizeModel.randomSeed = 0L
                    visualizeModel.userSeed = false
                    text = ""
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}
