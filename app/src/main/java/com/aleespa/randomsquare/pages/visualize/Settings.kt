package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.hslToColor
import com.aleespa.randomsquare.tools.toHsl
import kotlin.math.roundToInt


@Composable
fun BackgroundColorSelector(visualizeModel: VisualizeModel) {
    // Read initial HSL from model
    val initialHsl = remember(visualizeModel.bgColor) {
        Color(visualizeModel.bgColor).toHsl()
    }

    var hue by remember { mutableFloatStateOf(initialHsl[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsl[1]) }
    var lightness by remember { mutableFloatStateOf(initialHsl[2]) }

    val currentColor = hslToColor(hue, saturation, lightness)

    // Update the model only when sliders change
    LaunchedEffect(hue, saturation, lightness) {
        visualizeModel.bgColor = currentColor.toArgb()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        // Hue
        Text("Hue: ${hue.roundToInt()}")


        val hueGradientColors = (0..6).map { i ->
            val hueValue = i * 60f
            hslToColor(hueValue, saturation, lightness)
        } + listOf(hslToColor(0f, saturation, lightness)) // wrap around to red


        GradientSlider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            gradient = Brush.horizontalGradient(hueGradientColors),
            thumbColor = currentColor
        )

        Spacer(Modifier.height(12.dp))

        // Saturation

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
                thumbColor = Color.Transparent // we override it with a custom thumb
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .width(18.dp)   // narrow, vertical shape
                        .height(32.dp)  // taller than wide
                        .shadow(4.dp, RoundedCornerShape(6.dp))
                        .background(thumbColor, RoundedCornerShape(6.dp))
                        .border(
                            3.dp,
                            Color.White.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        ) // semi-transparent border
                )
            }
        )
    }
}

@Composable
fun ColormapDropdown(
    visualizeModel: VisualizeModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedColormap = visualizeModel.selectedColormap

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .widthIn(max = 200.dp) // max width
            .fillMaxWidth()        // center horizontally in parent
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
            Colormaps.entries.forEach { colormap ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(colormap.key, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(35.dp))

                            ColormapSineWaveLine(
                                colormap = colormap,
                                modifier = Modifier.size(width = 80.dp, height = 18.dp)
                            )

                        }
                    },
                    onClick = {
                        visualizeModel.selectedColormap = colormap
                        expanded = false
                    }
                )
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
                cap = StrokeCap.Round,   // makes the ends rounded
                join = StrokeJoin.Round  // makes corners/spline joins rounded
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
        modifier = Modifier
            .padding(horizontal = 95.dp),
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

