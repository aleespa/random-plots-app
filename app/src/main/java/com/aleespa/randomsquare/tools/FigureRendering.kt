package com.aleespa.randomsquare.tools

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.SketchRenderer

/**
 * Where a fractal or composition is looking, and the fractal-specific parameters.
 * [defaultsFor] is the single source of each figure's starting view (also used by
 * VisualizeModel.resetFractalSettings()).
 */
data class FractalView(
    val xCenter: Double = 0.0,
    val yCenter: Double = 0.0,
    val zoom: Double = 1.0,
    val iterations: Int = 150,
    val juliaCX: Double = -0.7,
    val juliaCY: Double = 0.27015,
    val multibrotD: Double = 3.0,
    val newtonCoeffs: DoubleArray = DoubleArray(9),
) {
    companion object {
        fun defaultsFor(figure: Figures) = FractalView(
            xCenter = if (figure == Figures.MANDELBROT) -1.0 else 0.0,
            iterations = when (figure) {
                Figures.MANDELBROT -> 100
                Figures.JULIA -> 80
                Figures.TRICORN -> 80
                Figures.MULTIBROT -> 100
                Figures.NEWTON -> 100
                else -> 150
            },
            newtonCoeffs = if (figure == Figures.NEWTON) {
                doubleArrayOf(-1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0) // z^3 - 1
            } else DoubleArray(9),
        )
    }
}

/** Everything needed to render one image of a figure, independent of UI state. */
data class RenderSpec(
    val figure: Figures,
    val seed: Long,
    val width: Int,
    val height: Int,
    val bgColor: Int = android.graphics.Color.BLACK,
    val colormap: Colormaps =
        if (figure.figureType == FigureType.FRACTAL) Colormaps.PLASMA_FRACTAL else Colormaps.RAINBOW,
    val fractal: FractalView = FractalView.defaultsFor(figure),
)

/**
 * Renders [spec] through the matching native engine (composition VM, fractal shader or Skia
 * sketch). Returns null when the engine fails, e.g. no OpenGL ES 3.1 for GPU figures.
 */
fun renderFigure(spec: RenderSpec): ImageBitmap? {
    val finalWidth = spec.width
    val finalHeight = spec.height

    if (spec.figure.figureType == FigureType.COMPOSITIONS) {
        val seed = spec.seed
        val rng = java.util.Random(seed)
        val k = when (spec.figure) {
            Figures.SUPER_RANDOM -> rng.nextInt(35) + 15
            Figures.SOFT -> rng.nextInt(7) + 8
            Figures.BLACKWHITE -> 10
            Figures.NOISE -> 25
            else -> 15
        }

        val opcodes = mutableListOf<Int>()
        val params = mutableListOf<Float>()

        // Op Definitions matching composition_shader.h
        // 0:X, 1:Y, 2:Const, 3:Sum, 4:Prod, 5:Mod, 6:Well, 7:Tent, 8:Sin, 9:Level, 10:Mix

        val ops0 = when (spec.figure) {
            Figures.BLACKWHITE -> listOf(0, 1) // X, Y
            else -> listOf(0, 1, 2) // X, Y, Constant
        }

        val ops1 = when (spec.figure) {
            Figures.SOFT -> listOf(3, 4, 9, 10) // Sum, Prod, Level, Mix
            Figures.BLACKWHITE -> listOf(
                3,
                4,
                5,
                6,
                7,
                8,
                10
            ) // Sum, Prod, Mod, Well, Tent, Sin, Mix
            Figures.NOISE -> listOf(3, 8) // Sum, Sin
            else -> listOf(3, 4, 5, 6, 7, 8, 9, 10) // All (Super)
        }

        fun generateBytecode(depth: Int) {
            if (depth <= 0) {
                val op = ops0[rng.nextInt(ops0.size)]
                opcodes.add(op)
                if (op == 2) {
                    params.add(rng.nextFloat() * 2 - 1)
                    params.add(rng.nextFloat() * 2 - 1)
                    params.add(rng.nextFloat() * 2 - 1)
                } else {
                    params.add(0f); params.add(0f); params.add(0f)
                }
            } else {
                val op = ops1[rng.nextInt(ops1.size)]
                val arity = when (op) {
                    6, 7, 8 -> 1
                    3, 4, 5 -> 2
                    9, 10 -> 3
                    else -> 1
                }

                // For a stack-based VM, we need postfix order
                if (arity == 1) {
                    generateBytecode(depth - 1)
                } else if (arity == 2) {
                    val split = if (depth > 0) rng.nextInt(depth) else 0
                    generateBytecode(split)
                    generateBytecode(depth - 1 - split)
                } else if (arity == 3) {
                    val s1 = if (depth > 0) rng.nextInt(depth) else 0
                    val s2 = if (depth > 0) rng.nextInt(depth) else 0
                    val sortedSplits = listOf(s1, s2).sorted()
                    generateBytecode(sortedSplits[0])
                    generateBytecode(sortedSplits[1] - sortedSplits[0])
                    generateBytecode(depth - 1 - sortedSplits[1])
                }

                opcodes.add(op)
                if (op == 8) { // Sin needs phase and freq
                    params.add(rng.nextFloat() * Math.PI.toFloat())
                    val freq = when (spec.figure) {
                        Figures.NOISE -> 2.0f + rng.nextFloat() * 19.0f
                        Figures.SOFT -> 1.0f + rng.nextFloat() * 11.0f
                        else -> 1.0f + rng.nextFloat() * 5.0f
                    }
                    params.add(freq)
                    params.add(0f)
                } else if (op == 9) { // Level needs threshold
                    params.add(rng.nextFloat() * 2 - 1)
                    params.add(0f); params.add(0f)
                } else {
                    params.add(0f); params.add(0f); params.add(0f)
                }
            }
        }

        generateBytecode(k)

        val imageBytes = FractalRenderer.renderComposition(
            finalWidth,
            finalHeight,
            spec.fractal.xCenter,
            spec.fractal.yCenter,
            spec.fractal.zoom,
            opcodes.toIntArray(),
            params.toFloatArray()
        ) ?: return null // GPU renderer unavailable (no OpenGL ES 3.1)
        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(imageBytes))
        return bitmap.asImageBitmap()
    }

    if (spec.figure.figureType == FigureType.FRACTAL) {
        val palSize = spec.colormap.colorlist.size
        val palT = FloatArray(palSize) { i -> i.toFloat() / (palSize - 1) }
        val palRGB = FloatArray(palSize * 3)
        spec.colormap.colorlist.forEachIndexed { i, color ->
            palRGB[i * 3 + 0] = ((color shr 16) and 0xFF) / 255.0f
            palRGB[i * 3 + 1] = ((color shr 8) and 0xFF) / 255.0f
            palRGB[i * 3 + 2] = (color and 0xFF) / 255.0f
        }

        val maxIter = spec.fractal.iterations

        val type = spec.figure.fractalType ?: return null
        val params = FloatArray(10)
        when (spec.figure) {
            Figures.MULTIBROT -> params[0] = spec.fractal.multibrotD.toFloat()
            Figures.NEWTON -> spec.fractal.newtonCoeffs.forEachIndexed { i, d ->
                params[i] = d.toFloat()
            }

            else -> {}
        }

        val imageBytes = FractalRenderer.render(
            type,
            finalWidth,
            finalHeight,
            maxIter,
            spec.fractal.xCenter,
            spec.fractal.yCenter,
            spec.fractal.zoom,
            spec.fractal.juliaCX,
            spec.fractal.juliaCY,
            params,
            palT,
            palRGB
        ) ?: return null // GPU renderer unavailable (no OpenGL ES 3.1)
        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val buffer = java.nio.ByteBuffer.wrap(imageBytes)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap.asImageBitmap()
    }

    val sketchId = spec.figure.sketchId ?: return null
    val result = SketchRenderer.renderBitmap(
        sketchId = sketchId,
        seed = spec.seed,
        width = finalWidth,
        height = finalHeight,
        bgColor = spec.bgColor,
        colormap = spec.colormap
    )
    return result?.asImageBitmap()
}
