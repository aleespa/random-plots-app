package com.aleespa.randomsquare.tools

/**
 * GPU renderers (OpenGL ES 3.1 compute shaders). Every render call returns null when the native
 * side fails, e.g. when EGL can't create an ES 3.1 context on devices/emulators without it.
 */
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
                System.loadLibrary("randomsquare")
            } catch (e: UnsatisfiedLinkError) {
                // Log or handle the error if not in a test environment
            }
        }
    }

    external fun renderInternal(
        type: Int, width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        cx: Double, cy: Double,
        params: FloatArray
    ): ByteArray?

    /**
     * Renders a fractal as a colour field (RGBA8: R/G = palette position t, B = shade, A = alpha);
     * decode it with ColorField.fromRgbaBytes and colour it in Kotlin.
     */
    fun render(
        type: Int, width: Int, height: Int, maxIter: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        cx: Double, cy: Double,
        params: FloatArray
    ): ByteArray? = renderInternal(type, width, height, maxIter, xCenter, yCenter, zoom, cx, cy, params)

    external fun renderCompositionInternal(
        width: Int, height: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        opcodes: IntArray, params: FloatArray
    ): ByteArray?

    fun renderComposition(
        width: Int, height: Int,
        xCenter: Double, yCenter: Double, zoom: Double,
        opcodes: IntArray, params: FloatArray
    ): ByteArray? = renderCompositionInternal(width, height, xCenter, yCenter, zoom, opcodes, params)
}