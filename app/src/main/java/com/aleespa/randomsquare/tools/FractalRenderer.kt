package com.aleespa.randomsquare.tools

object FractalRenderer {
    init {
        System.loadLibrary("fractalrenderer")
    }

    external fun renderInternal(
        type: Int, width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        cx: Double, cy: Double,
        params: FloatArray,
        palT: FloatArray, palRGB: FloatArray
    ): ByteArray

    fun renderMandelbrot(
        width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        palT: FloatArray, palRGB: FloatArray
    ): ByteArray = renderInternal(
        0, width, height, maxIter, xCenter, yCenter, zoom, 0.0, 0.0, FloatArray(10), palT, palRGB
    )

    fun renderJulia(
        width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        cx: Double, cy: Double,
        palT: FloatArray, palRGB: FloatArray
    ): ByteArray = renderInternal(
        1, width, height, maxIter, xCenter, yCenter, zoom, cx, cy, FloatArray(10), palT, palRGB
    )

    external fun renderComposition(
        width: Int, height: Int, opcodes: IntArray, params: FloatArray
    ): ByteArray
}