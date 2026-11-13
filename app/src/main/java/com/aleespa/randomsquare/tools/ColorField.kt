package com.aleespa.randomsquare.tools

import android.graphics.Bitmap
import com.aleespa.randomsquare.Colormaps

/**
 * A rendered image before colouring: per pixel a palette position t (16 bits), a shade (8 bits)
 * and a coverage alpha (8 bits), packed as `t << 16 | shade << 8 | alpha` (see
 * RenderContext::enableFieldMode and packField in fractal_shader.h).
 *
 * Fractals and the Figures.rendersColorField sketches render to this, so changing the colormap or
 * background only re-runs [toBitmap] (a lookup per pixel) instead of the native render.
 */
class ColorField(val width: Int, val height: Int, private val pixels: IntArray) {

    /** colour = colormap(t) * shade, blended over [bgColor] by alpha. */
    fun toBitmap(colormap: Colormaps, bgColor: Int): Bitmap {
        val lut = lutFor(colormap)
        val bgR = (bgColor shr 16) and 0xFF
        val bgG = (bgColor shr 8) and 0xFF
        val bgB = bgColor and 0xFF
        val opaqueBg = bgColor or OPAQUE
        val out = IntArray(pixels.size)
        for (i in pixels.indices) {
            val p = pixels[i]
            val alpha = p and 0xFF
            if (alpha == 0) {
                out[i] = opaqueBg
                continue
            }
            val c = lut[p ushr (32 - LUT_BITS)]
            val shade = (p shr 8) and 0xFF
            if (alpha == 255 && shade == 255) {
                out[i] = c
                continue
            }
            val k = shade * alpha // src weight, scaled by 255 * 255
            val inv = (255 - alpha) * 255 // bg weight, same scale
            val r = (((c shr 16) and 0xFF) * k + bgR * inv) / 65025
            val g = (((c shr 8) and 0xFF) * k + bgG * inv) / 65025
            val b = ((c and 0xFF) * k + bgB * inv) / 65025
            out[i] = OPAQUE or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(out, width, height, Bitmap.Config.ARGB_8888)
    }

    companion object {
        private const val OPAQUE = 0xFF000000.toInt()
        private const val LUT_BITS = 10

        /** From the fractal shader's RGBA8 readback: R/G = t (high/low byte), B = shade, A = alpha. */
        fun fromRgbaBytes(width: Int, height: Int, bytes: ByteArray): ColorField? {
            if (bytes.size < width * height * 4) return null
            val pixels = IntArray(width * height) { i ->
                val o = i * 4
                ((bytes[o].toInt() and 0xFF) shl 24) or ((bytes[o + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[o + 2].toInt() and 0xFF) shl 8) or (bytes[o + 3].toInt() and 0xFF)
            }
            return ColorField(width, height, pixels)
        }

        private val luts = HashMap<Colormaps, IntArray>()

        /** 1024-entry colormap table; same interpolation as sampleColormap in cpp/colormap.h. */
        @Synchronized
        private fun lutFor(colormap: Colormaps): IntArray = luts.getOrPut(colormap) {
            val stops = colormap.colorlist
            val last = stops.size - 1
            IntArray(1 shl LUT_BITS) { k ->
                val t = k / ((1 shl LUT_BITS) - 1).toFloat()
                val pos = t * last
                val i = minOf(last - 1, pos.toInt()).coerceAtLeast(0)
                val f = if (last == 0) 0f else pos - i
                val a = stops[i]
                val b = stops[minOf(last, i + 1)]
                fun ch(shift: Int): Int {
                    val ca = (a shr shift) and 0xFF
                    val cb = (b shr shift) and 0xFF
                    return (ca + (cb - ca) * f).toInt()
                }
                OPAQUE or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
            }
        }
    }
}

/**
 * Keeps the last rendered [ColorField] so re-colouring the same image (same figure, seed, size and
 * view) skips the native render. One entry is enough: the UI only ever recolours the current image.
 */
internal object ColorFieldCache {
    private var key: Any? = null
    private var field: ColorField? = null

    @Synchronized
    fun getOrRender(key: Any, render: () -> ColorField?): ColorField? {
        if (key == this.key) field?.let { return it }
        val rendered = render() ?: return null
        this.key = key
        field = rendered
        return rendered
    }

    @Synchronized
    fun clear() {
        key = null
        field = null
    }
}
