package com.aleespa.randomsquare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColormapAvailabilityTest {

    @Test
    fun prismOffersFullColorButNoSolidColors() {
        assertTrue(Colormaps.FULL_COLOR.isAvailableFor(Figures.PRISM))
        assertTrue(Colormaps.INFERNO.isAvailableFor(Figures.PRISM))
        assertFalse(Colormaps.SOLID_BLACK.isAvailableFor(Figures.PRISM))
        assertFalse(Colormaps.SOLID_WHITE.isAvailableFor(Figures.PRISM))
        assertFalse(Colormaps.PLASMA_FRACTAL.isAvailableFor(Figures.PRISM))
    }

    @Test
    fun otherSketchesKeepSolidColorsAndNoFullColor() {
        assertTrue(Colormaps.SOLID_BLACK.isAvailableFor(Figures.SPIRAL))
        assertFalse(Colormaps.FULL_COLOR.isAvailableFor(Figures.SPIRAL))
    }

    @Test
    fun fractalsOnlyOfferFractalColormaps() {
        Colormaps.entries.forEach {
            assertEquals(it.name, it.isFractalSpecific, it.isAvailableFor(Figures.MANDELBROT))
        }
    }

    @Test
    fun everyFiguresDefaultColormapIsAvailableForIt() {
        Figures.entries.forEach { figure ->
            assertTrue(figure.name, Colormaps.defaultFor(figure).isAvailableFor(figure))
        }
        assertEquals(Colormaps.FULL_COLOR, Colormaps.defaultFor(Figures.PRISM))
    }
}
