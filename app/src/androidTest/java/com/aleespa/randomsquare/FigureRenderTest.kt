package com.aleespa.randomsquare

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.aleespa.randomsquare.tools.RenderSpec
import com.aleespa.randomsquare.tools.renderFigure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

/**
 * Renders every figure through the real native engines on a device. Catches what the JVM unit
 * tests can't: a `Figures.sketchId` / `fractalType` that doesn't match the C++ side, native
 * crashes, and figures that come out blank.
 *
 * Needs a device or emulator with OpenGL ES 3.1 (fractals and compositions run on the GPU).
 *
 * With the instrumentation argument `exportSamples=true` it also writes each render as a
 * 1000x1000 WebP Browse sample to the app's external files dir; see scripts/export-figure-samples.sh.
 */
@RunWith(Parameterized::class)
class FigureRenderTest(private val figure: Figures) {

    companion object {
        private const val SIZE = 600
        private const val SAMPLE_SIZE = 1000
        private const val SEED = 20251112L

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun figures(): List<Figures> = Figures.entries
    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val exportSamples =
        InstrumentationRegistry.getArguments().getString("exportSamples") == "true"

    @Test
    fun rendersANonBlankImage() {
        val bitmap = render(SIZE)
        assertEquals("${figure.name} width", SIZE, bitmap.width)
        assertEquals("${figure.name} height", SIZE, bitmap.height)

        val colors = distinctSampledColors(bitmap)
        assertTrue(
            "${figure.name} looks blank: only $colors distinct colors on a 32x32 sample grid",
            colors >= 4
        )

        if (exportSamples) export(render(SAMPLE_SIZE))
    }

    private fun render(size: Int): Bitmap {
        val image = renderFigure(RenderSpec(figure = figure, seed = SEED, width = size, height = size))
        assertNotNull(
            "${figure.name} rendered null: check its sketchId/fractalType against the C++ " +
                "switch / u_type, and that the device has OpenGL ES 3.1",
            image
        )
        return image!!.asAndroidBitmap()
    }

    private fun distinctSampledColors(bitmap: Bitmap): Int {
        val colors = HashSet<Int>()
        val step = bitmap.width / 32
        for (y in step / 2 until bitmap.height step step) {
            for (x in step / 2 until bitmap.width step step) {
                colors += bitmap.getPixel(x, y)
            }
        }
        return colors.size
    }

    private fun export(bitmap: Bitmap) {
        val dir = File(context.getExternalFilesDir(null), "figure-samples").apply { mkdirs() }
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        }
        File(dir, "${figure.key}.webp").outputStream().use { bitmap.compress(format, 85, it) }
    }
}
