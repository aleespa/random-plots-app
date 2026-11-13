package com.aleespa.randomsquare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The native IDs on [Figures] must match the C++ side: `sketchId` is the `switch` in
 * cpp/jni_bridge.cpp, `fractalType` is `u_type` in cpp/fractal_shader.h.
 */
class FiguresTest {

    private val sketches = Figures.entries.filter { it.sketchId != null }

    @Test
    fun everyFigureUsesExactlyOneEngine() {
        Figures.entries.forEach { figure ->
            assertFalse(
                "${figure.name} has both a sketchId and a fractalType",
                figure.sketchId != null && figure.fractalType != null
            )
            // Outside Fractals and Compositions, every figure is a Skia sketch. Compositions are
            // either GPU-VM compositions (no ID) or C++ sketches such as Prism.
            if (figure.figureType != FigureType.FRACTAL && figure.figureType != FigureType.COMPOSITIONS) {
                assertNotNull("${figure.name} needs a sketchId", figure.sketchId)
            }
        }
    }

    @Test
    fun sketchIdsAreUniqueAndContiguousFromZero() {
        val ids = sketches.map { it.sketchId!! }.sorted()
        assertEquals((0 until sketches.size).toList(), ids)
    }

    @Test
    fun fractalsAndAllCompositionsHaveAPanZoomView() {
        assertTrue(Figures.MANDELBROT.hasPanZoomView)
        assertTrue(Figures.SUPER_RANDOM.hasPanZoomView)
        assertTrue("Prism pans/zooms through the sketch view", Figures.PRISM.hasPanZoomView)
        assertFalse(Figures.SPIRAL.hasPanZoomView)
    }

    @Test
    fun everyFractalHasAUniqueFractalType_andOnlyFractalsDo() {
        Figures.entries.forEach { figure ->
            if (figure.figureType == FigureType.FRACTAL) {
                assertNotNull("${figure.name} needs a fractalType", figure.fractalType)
            } else {
                assertNull("${figure.name} is not a shader fractal", figure.fractalType)
            }
        }
        val types = Figures.entries.mapNotNull { it.fractalType }
        assertEquals(types.size, types.toSet().size)
    }

    /** Regression guard: these values are hard-coded in the native code. */
    @Test
    fun existingNativeIdsAreUnchanged() {
        val expectedSketchIds = mapOf(
            Figures.NOISY_CIRCLES to 0,
            Figures.BUBBLES to 1,
            Figures.WAVES to 2,
            Figures.CONSTELLATIONS to 3,
            Figures.POLYGON_GRID to 4,
            Figures.SPIRAL to 5,
            Figures.CUBISM to 6,
            Figures.EXPONENTIAL_SUM to 7,
            Figures.POLYGON_FEEDBACK to 8,
            Figures.POLYGON_TUNNEL to 9,
            Figures.ROTATIONS to 10,
            Figures.ORBITS to 11,
            Figures.SPIROGRAPH to 12,
            Figures.CONTINUOUS_SPIROGRAPH to 13,
            Figures.RANDOM_EIGENVALUES to 14,
            Figures.PRISM to 15,
        )
        expectedSketchIds.forEach { (figure, id) -> assertEquals(figure.name, id, figure.sketchId) }

        val expectedFractalTypes = mapOf(
            Figures.MANDELBROT to 0,
            Figures.JULIA to 1,
            Figures.TRICORN to 2,
            Figures.MULTIBROT to 3,
            Figures.NEWTON to 4,
        )
        expectedFractalTypes.forEach { (figure, type) ->
            assertEquals(figure.name, type, figure.fractalType)
        }
    }
}
