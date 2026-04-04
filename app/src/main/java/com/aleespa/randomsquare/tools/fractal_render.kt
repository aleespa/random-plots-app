package com.aleespa.randomsquare.tools

object FractalRenderer {
    init {
        System.loadLibrary("fractalrenderer")
    }

    external fun renderMandelbrot(
        width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        palT: FloatArray, palRGB: FloatArray
    ): ByteArray

    external fun renderJulia(
        width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        cx: Double, cy: Double,
        palT: FloatArray, palRGB: FloatArray
    ): ByteArray

    external fun renderComposition(
        width: Int, height: Int, opcodes: IntArray, params: FloatArray
    ): ByteArray
}