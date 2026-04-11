package com.aleespa.randomsquare

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color

object SketchRenderer {

    init {
        System.loadLibrary("randomsquare")
    }

    external fun renderSketch(
        sketchId: Int,
        seed: Long,
        width: Int,
        height: Int,
        bgColor: Int,
        colormapT: FloatArray,
        colormapRGB: IntArray
    ): ByteArray?

    fun renderBitmap(
        sketchId: Int,
        seed: Long,
        width: Int = 1200,
        height: Int = 1200,
        bgColor: Int = Color.BLACK,
        colormap: Colormaps
    ): Bitmap? {
        val palSize = colormap.colorlist.size
        val palT = FloatArray(palSize) { i -> i.toFloat() / (palSize - 1) }
        val palRGB = IntArray(palSize)
        colormap.colorlist.forEachIndexed { i, color ->
            palRGB[i] = color
        }

        val bytes = renderSketch(sketchId, seed, width, height, bgColor, palT, palRGB) ?: return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
