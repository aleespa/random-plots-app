package com.aleespa.randomsquare.data

import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.Figures
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisualizeModelTest {

    private lateinit var model: VisualizeModel
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val settingsRepository: AppSettingsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        model = VisualizeModel(imageRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testJuliaPolarConversion() {
        // Test positive y (theta in [0, PI])
        model.juliaCX = 0.0
        model.juliaCY = 1.0
        model.updatePolarFromJulia()
        assertEquals(1.0, model.juliaR, 0.001)
        assertEquals(Math.PI / 2.0, model.juliaTheta.toDouble(), 0.001)

        // Test negative y (theta would be negative from atan2, should be normalized to [PI, 2PI])
        model.juliaCX = 0.0
        model.juliaCY = -1.0
        model.updatePolarFromJulia()
        assertEquals(1.0, model.juliaR, 0.001)
        assertEquals(3.0 * Math.PI / 2.0, model.juliaTheta.toDouble(), 0.001)

        // Test 0,0
        model.juliaCX = 0.0
        model.juliaCY = 0.0
        model.updatePolarFromJulia()
        assertEquals(0.0, model.juliaR, 0.001)
        assertEquals(0.0, model.juliaTheta.toDouble(), 0.001)

        model.juliaR = 2.0
        model.juliaTheta = (Math.PI / 2.0)
        model.updateJuliaFromPolar()
        
        assertEquals(0.0, model.juliaCX, 0.001)
        assertEquals(2.0, model.juliaCY, 0.001)
    }

    @Test
    fun testSelectedFigureChangeResetsFractal() {
        // Change to a non-fractal first
        model.selectedFigure = Figures.SPIROGRAPH
        
        // Setup some fractal settings
        model.fractalZoom = 5.0
        model.fractalXCenter = 10.0
        
        // Switch to a fractal
        model.selectedFigure = Figures.MANDELBROT
        
        // Should be reset
        assertEquals(1.0, model.fractalZoom, 0.0)
        assertEquals(-1.0, model.fractalXCenter, 0.0)
    }

    @Test
    fun testColormapSwitchingOnFigureChange() {
        // From non-fractal to fractal
        model.selectedFigure = Figures.SPIROGRAPH
        model.selectedColormap = Colormaps.RAINBOW
        
        model.selectedFigure = Figures.MANDELBROT
        assertTrue(model.selectedColormap.isFractalSpecific)
        
        // From fractal to non-fractal
        model.selectedFigure = Figures.SPIROGRAPH
        assertFalse(model.selectedColormap.isFractalSpecific)
    }

    @Test
    fun testImageFilterConditions() {
        model.darkFilter = true
        model.lightFilter = false
        model.filterImageType = "mandelbrot"
        
        val conditions = model.getImageFilterConditions()
        assertEquals(true, conditions.isDarkMode)
        assertEquals("mandelbrot", conditions.imageType)
        
        model.darkFilter = false
        model.lightFilter = true
        model.filterImageType = "None"
        
        val conditions2 = model.getImageFilterConditions()
        assertEquals(false, conditions2.isDarkMode)
        assertEquals(null, conditions2.imageType)
    }
}
