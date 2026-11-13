package com.aleespa.randomsquare.pages.visualize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.generateNewPlot
import com.aleespa.randomsquare.tools.hslToColor
import com.aleespa.randomsquare.tools.toHsl
import kotlin.math.roundToInt
import com.aleespa.randomsquare.ui.theme.Sizes
import com.aleespa.randomsquare.R
import com.aleespa.randomsquare.displayName
import androidx.compose.ui.res.stringResource
import com.aleespa.randomsquare.ui.theme.Spacing

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
            .padding(horizontal = Spacing.screenGutter),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        val hueGradientColors = (0..6).map { i ->
            val hueValue = i * 60f
            hslToColor(hueValue, saturation, lightness)
        } + listOf(hslToColor(0f, saturation, lightness))

        LabeledGradientSlider(
            label = stringResource(R.string.hue_label, hue.roundToInt()),
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            gradient = Brush.horizontalGradient(hueGradientColors),
            thumbColor = currentColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            LabeledGradientSlider(
                label = stringResource(R.string.saturation_label, (saturation * 100).roundToInt()),
                value = saturation,
                onValueChange = { saturation = it },
                valueRange = 0f..1f,
                gradient = Brush.horizontalGradient(
                    listOf(
                        hslToColor(hue, 0f, lightness),
                        hslToColor(hue, 1f, lightness)
                    )
                ),
                thumbColor = currentColor,
                modifier = Modifier.weight(1f)
            )
            LabeledGradientSlider(
                label = stringResource(R.string.lightness_label, (lightness * 100).roundToInt()),
                value = lightness,
                onValueChange = { lightness = it },
                valueRange = 0f..1f,
                gradient = Brush.horizontalGradient(
                    listOf(Color.Black, hslToColor(hue, saturation, 0.5f), Color.White)
                ),
                thumbColor = currentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Left-aligned label above a [GradientSlider]: the one slider pattern used on Visualize. */
@Composable
fun LabeledGradientSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    gradient: Brush,
    thumbColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        GradientSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            gradient = gradient,
            thumbColor = thumbColor
        )
    }
}

@Composable
fun BackgroundColorSelector(visualizeModel: VisualizeModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    BackgroundColorSelector(
        bgColor = visualizeModel.bgColor,
        onColorChange = {
            if (visualizeModel.bgColor != it) {
                visualizeModel.bgColor = it
                generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
            }
        }
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
        contentAlignment = Alignment.Center
    ) {
        val sliderState = remember(valueRange) { SliderState(value, trackRange = valueRange) }
        sliderState.value = value

        Slider(
            state = sliderState,
            onValueChange = onValueChange,
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
    onShowDialogChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = Sizes.maxControlWidth)
            .padding(horizontal = Spacing.screenGutter)
            .fillMaxWidth()
    ) {
        Surface(
            onClick = { onShowDialogChange(true) },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedColormap.displayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColormapSineWaveLine(
                    colormap = selectedColormap,
                    modifier = Modifier.size(width = 80.dp, height = 24.dp)
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
    val isFractal =
        visualizeModel.selectedFigure.figureType == FigureType.FRACTAL
    ColormapDropdown(
        selectedColormap = visualizeModel.selectedColormap,
        isFractal = isFractal,
        onShowDialogChange = { visualizeModel.showColormapDialog = it },
        modifier = modifier
    )
}

@Composable
fun FractalSettings(visualizeModel: VisualizeModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Same slider style as the background color controls; numeric parameters get a neutral
    // theme gradient instead of a color one.
    val trackGradient = Brush.horizontalGradient(
        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary)
    )
    val thumbColor = MaterialTheme.colorScheme.primary
    val regenerate = {
        generateNewPlot(visualizeModel, context, randomizeSeed = false, showAds = false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenGutter),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        val iterationRange = when (visualizeModel.selectedFigure) {
            Figures.MANDELBROT -> 50f..1000f
            Figures.TRICORN -> 30f..300f
            Figures.JULIA -> 50f..1000f
            Figures.MULTIBROT -> 20f..500f
            Figures.NEWTON -> 10f..100f
            Figures.LYAPUNOV -> 50f..1000f
            else -> 50f..2000f
        }

        LabeledGradientSlider(
            label = stringResource(R.string.iterations_label, visualizeModel.fractalIterations),
            value = visualizeModel.fractalIterations.toFloat().coerceIn(iterationRange),
            onValueChange = {
                visualizeModel.fractalIterations = it.toInt()
                regenerate()
            },
            valueRange = iterationRange,
            gradient = trackGradient,
            thumbColor = thumbColor
        )

        if (visualizeModel.selectedFigure == Figures.JULIA) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                LabeledGradientSlider(
                    label = "r: ${String.format("%.3f", visualizeModel.juliaR)}",
                    value = visualizeModel.juliaR.toFloat(),
                    onValueChange = {
                        visualizeModel.juliaR = it.toDouble()
                        visualizeModel.updateJuliaFromPolar()
                        regenerate()
                    },
                    valueRange = 0f..2f,
                    gradient = trackGradient,
                    thumbColor = thumbColor,
                    modifier = Modifier.weight(1f)
                )
                LabeledGradientSlider(
                    label = "θ: ${String.format("%.3f", visualizeModel.juliaTheta)}",
                    value = visualizeModel.juliaTheta.toFloat(),
                    onValueChange = {
                        visualizeModel.juliaTheta = it.toDouble()
                        visualizeModel.updateJuliaFromPolar()
                        regenerate()
                    },
                    valueRange = 0f..(2 * Math.PI).toFloat(),
                    gradient = trackGradient,
                    thumbColor = thumbColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (visualizeModel.selectedFigure == Figures.MULTIBROT) {
            LabeledGradientSlider(
                label = stringResource(R.string.power_label, String.format("%.2f", visualizeModel.multibrotD)),
                value = visualizeModel.multibrotD.toFloat(),
                onValueChange = {
                    visualizeModel.multibrotD = it.toDouble()
                    regenerate()
                },
                valueRange = 2f..10f,
                gradient = trackGradient,
                thumbColor = thumbColor
            )
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

        // Draw shadow/shade
        translate(top = 2f, left = 2f) {
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.25f),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = Sizes.maxControlWidth)
            .padding(horizontal = Spacing.screenGutter),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(stringResource(R.string.seed)) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    visualizeModel.randomSeed = 0L
                    visualizeModel.userSeed = false
                    text = ""
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            }
        }
    )
}
