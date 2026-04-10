package com.aleespa.randomsquare.tools

object FractalRenderer {
    init {
        loadNativeLibrary()
    }

    private fun loadNativeLibrary() {
        val isTesting = try {
            System.getProperty("is_testing") == "true"
        } catch (e: Exception) {
            false
        }
        if (!isTesting) {
            try {
                System.loadLibrary("fractalrenderer")
            } catch (e: UnsatisfiedLinkError) {
                // Log or handle the error if not in a test environment
            }
        }
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