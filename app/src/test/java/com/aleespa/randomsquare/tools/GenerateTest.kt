package com.aleespa.randomsquare.tools

import android.content.Context
import android.graphics.Bitmap
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.SketchRenderer
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.ImageRepository
import com.aleespa.randomsquare.data.VisualizeModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GenerateTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            System.setProperty("is_testing", "true")
        }
    }

    private val context: Context = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val settingsRepository: AppSettingsRepository = mockk(relaxed = true)
    private lateinit var visualizeModel: VisualizeModel

    @Before
    fun setup() {
        // Mock all native-loading objects BEFORE they are accessed
        ColorFieldCache.clear()
        mockkObject(FractalRenderer)
        every { FractalRenderer["loadNativeLibrary"]() } returns Unit
        every { FractalRenderer.render(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns ByteArray(100 * 100 * 4)
        every { FractalRenderer.renderComposition(any(), any(), any(), any(), any(), any(), any()) } returns ByteArray(100 * 100 * 4)

        mockkObject(SketchRenderer)
        try {
            every { SketchRenderer["loadNativeLibrary"]() } returns Unit
        } catch (e: Exception) {}
        every { SketchRenderer.renderBitmap(any(), any(), any(), any(), any(), any()) } returns mockk<Bitmap>(relaxed = true)

        every { settingsRepository.darkModeSetting } returns kotlinx.coroutines.flow.flowOf(com.aleespa.randomsquare.data.SettingDarkMode.Auto)
        every { settingsRepository.selectedFigure } returns kotlinx.coroutines.flow.flowOf(Figures.POLYGON_FEEDBACK)
        every { settingsRepository.selectedColormapColors } returns kotlinx.coroutines.flow.flowOf(emptyList())
        every { settingsRepository.bgColor } returns kotlinx.coroutines.flow.flowOf(0)
        // Stub every flow VisualizeModel collects: a relaxed-mock Flow makes MockK run Kotlin
        // reflection, which is extremely slow (minutes, then OOM) under Robolectric.
        every { settingsRepository.themeSource } returns kotlinx.coroutines.flow.emptyFlow()
        every { settingsRepository.imageResolution } returns kotlinx.coroutines.flow.emptyFlow()

        visualizeModel = VisualizeModel(imageRepository, settingsRepository)
        // Set a default colormap that isn't fractal-specific to avoid automatic changes during tests
        visualizeModel.selectedColormap = Colormaps.RAINBOW
    }

    @After
    fun tearDown() {
        unmockkAll()
        System.clearProperty("is_testing")
    }

    @Test
    fun testGenerateRandomPlot_SkiaBackend_AllFigures() {
        val skiaFigures = listOf(
            Figures.NOISY_CIRCLES,
            Figures.BUBBLES,
            Figures.WAVES,
            Figures.CONSTELLATIONS,
            Figures.POLYGON_GRID,
            Figures.SPIRAL,
            Figures.CUBISM,
            Figures.EXPONENTIAL_SUM,
            Figures.POLYGON_FEEDBACK,
            Figures.POLYGON_TUNNEL,
            Figures.ROTATIONS,
            Figures.ORBITS,
            Figures.SPIROGRAPH,
            Figures.CONTINUOUS_SPIROGRAPH,
            Figures.RANDOM_EIGENVALUES
        )

        for (figure in skiaFigures) {
            visualizeModel.selectedFigure = figure
            visualizeModel.randomSeed = 12345L
            visualizeModel.bgColor = 0xFF000000.toInt()
            
            // Re-set colormap AFTER setting the figure, because changing the figure type 
            // (e.g. from Fractal to Sketch) might trigger an automatic colormap change in the ViewModel.
            visualizeModel.selectedColormap = Colormaps.VIRIDIS

            val result = generateRandomPlot(visualizeModel, 100, 100)

            assertNotNull("Failed for figure: ${figure.key}", result)
            
            val expectedSketchId = when (figure) {
                Figures.NOISY_CIRCLES -> 0
                Figures.BUBBLES -> 1
                Figures.WAVES -> 2
                Figures.CONSTELLATIONS -> 3
                Figures.POLYGON_GRID -> 4
                Figures.SPIRAL -> 5
                Figures.CUBISM -> 6
                Figures.EXPONENTIAL_SUM -> 7
                Figures.POLYGON_FEEDBACK -> 8
                Figures.POLYGON_TUNNEL -> 9
                Figures.ROTATIONS -> 10
                Figures.ORBITS -> 11
                Figures.SPIROGRAPH -> 12
                Figures.CONTINUOUS_SPIROGRAPH -> 13
                Figures.RANDOM_EIGENVALUES -> 14
                else -> -1
            }

            verify {
                SketchRenderer.renderBitmap(
                    sketchId = expectedSketchId,
                    seed = 12345L,
                    width = 100,
                    height = 100,
                    bgColor = 0xFF000000.toInt(),
                    colormap = Colormaps.VIRIDIS
                )
            }
        }
    }

    @Test
    fun testGenerateRandomPlot_FractalCppBackend() {
        val width = 100
        val height = 100
        val fakeImageBytes = ByteArray(width * height * 4)
        
        every { 
            FractalRenderer.render(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } returns fakeImageBytes

        visualizeModel.selectedFigure = Figures.MANDELBROT
        visualizeModel.fractalIterations = 50
        visualizeModel.fractalXCenter = -0.5
        visualizeModel.fractalYCenter = 0.0
        visualizeModel.fractalZoom = 1.0

        val result = generateRandomPlot(visualizeModel, width, height)

        assertNotNull(result)
        verify { 
            FractalRenderer.render(
                0, // Mandelbrot type
                width,
                height,
                50,
                -0.5,
                0.0,
                1.0,
                any(),
                any(),
                any()
            ) 
        }
    }

    /** Changing only the colormap recolours the cached colour field instead of re-rendering. */
    @Test
    fun testColormapChangeReusesTheRenderedField() {
        visualizeModel.selectedFigure = Figures.MANDELBROT
        visualizeModel.selectedColormap = Colormaps.PLASMA_FRACTAL
        assertNotNull(generateRandomPlot(visualizeModel, 100, 100))
        visualizeModel.selectedColormap = Colormaps.MAGMA_FRACTAL
        assertNotNull(generateRandomPlot(visualizeModel, 100, 100))

        verify(exactly = 1) {
            FractalRenderer.render(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun testGenerateRandomPlot_CompositionCppBackend() {
        val width = 100
        val height = 100
        val fakeImageBytes = ByteArray(width * height * 4)
        
        every { 
            FractalRenderer.renderComposition(any(), any(), any(), any(), any(), any(), any()) 
        } returns fakeImageBytes

        visualizeModel.selectedFigure = Figures.SOFT
        visualizeModel.randomSeed = 999L

        val result = generateRandomPlot(visualizeModel, width, height)

        assertNotNull(result)
        verify {
            FractalRenderer.renderComposition(width, height, any(), any(), any(), any(), any())
        }
    }

    // The native renderer returns null when the device has no OpenGL ES 3.1 (e.g. EGL init fails).
    // That must surface as a null plot, not an NPE that crashes the app.
    @Test
    fun testGenerateRandomPlot_FractalRendererUnavailable_ReturnsNull() {
        every {
            FractalRenderer.render(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns null

        visualizeModel.selectedFigure = Figures.MANDELBROT

        assertNull(generateRandomPlot(visualizeModel, 100, 100))
    }

    @Test
    fun testGenerateRandomPlot_CompositionRendererUnavailable_ReturnsNull() {
        every {
            FractalRenderer.renderComposition(any(), any(), any(), any(), any(), any(), any())
        } returns null

        visualizeModel.selectedFigure = Figures.SOFT

        assertNull(generateRandomPlot(visualizeModel, 100, 100))
    }
}
