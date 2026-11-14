package com.aleespa.randomsquare.tools

import com.aleespa.randomsquare.data.ImageEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LastRendersTest {
    @Test
    fun settingsSurviveTheRoundTrip() {
        val fractal = ImageEntity(
            imageType = "julia", timestamp = 42L, isDarkMode = true, randomSeed = null,
            backgroundColor = 0xFF102030.toInt(), fractalXCenter = -0.5, fractalYCenter = 0.25,
            fractalZoom = 1e-3, param1 = -0.7, param2 = 0.27, param9 = 1.0,
            colormap = "magma", iterations = 300,
        )
        val sketch = ImageEntity(imageType = "spirograph", randomSeed = 4_000_000_000L, colormap = null)
        assertEquals(fractal, imageEntityFromJson(fractal.toJson()))
        assertEquals(sketch, imageEntityFromJson(sketch.toJson()))
    }
}
