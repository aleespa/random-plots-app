package com.aleespa.randomsquare.tools

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.ImageRepository
import com.aleespa.randomsquare.data.VisualizeModel
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.nio.ByteBuffer

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
        every { settingsRepository.darkModeSetting } returns kotlinx.coroutines.flow.flowOf(com.aleespa.randomsquare.data.SettingDarkMode.Auto)
        every { settingsRepository.selectedFigure } returns kotlinx.coroutines.flow.flowOf(Figures.POLYGON_FEEDBACK)
        every { settingsRepository.selectedColormapColors } returns kotlinx.coroutines.flow.flowOf(emptyList())
        every { settingsRepository.bgColor } returns kotlinx.coroutines.flow.flowOf(0)

        mockkObject(FractalRenderer)
        every { FractalRenderer["loadNativeLibrary"]() } returns Unit
        every { FractalRenderer.renderInternal(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns ByteArray(100 * 100 * 4)
        every { FractalRenderer.renderComposition(any(), any(), any(), any()) } returns ByteArray(100 * 100 * 4)

        mockkStatic(Python::class)
        every { Python.isStarted() } returns true

        visualizeModel = VisualizeModel(imageRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        System.clearProperty("is_testing")
    }

    @Test
    fun testGenerateRandomPlot_PythonBackend() {
        val python = mockk<Python>()
        val mainModule = mockk<PyObject>()
        val pyResult = mockk<PyObject>()
        
        every { Python.getInstance() } returns python
        every { python.getModule("main") } returns mainModule
        
        // Create a fake small PNG byte array
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        
        every { mainModule.callAttr("generate", any(), any(), any(), any()) } returns pyResult
        every { pyResult.toJava(ByteArray::class.java) } returns byteArray

        visualizeModel.selectedFigure = Figures.SPIROGRAPH // Non-fractal, uses Python
        visualizeModel.randomSeed = 12345L
        visualizeModel.bgColor = 0xFFFFFFFF.toInt()
        visualizeModel.selectedColormap = Colormaps.RAINBOW

        val result = generateRandomPlot(visualizeModel, 100, 100)

        assertNotNull(result)
        verify { mainModule.callAttr("generate", 12345L, any(), "spirograph", any()) }
    }

    @Test
    fun testGenerateRandomPlot_FractalCppBackend() {
        val width = 100
        val height = 100
        val fakeImageBytes = ByteArray(width * height * 4)
        
        every { 
            FractalRenderer.renderInternal(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } returns fakeImageBytes

        visualizeModel.selectedFigure = Figures.MANDELBROT
        visualizeModel.fractalIterations = 50
        visualizeModel.fractalXCenter = -0.5
        visualizeModel.fractalYCenter = 0.0
        visualizeModel.fractalZoom = 1.0

        val result = generateRandomPlot(visualizeModel, width, height)

        assertNotNull(result)
        verify { 
            FractalRenderer.renderInternal(
                0, // Mandelbrot type
                width,
                height,
                50,
                -0.5,
                0.0,
                1.0,
                any(),
                any(),
                any(),
                any(),
                any()
            ) 
        }
    }

    @Test
    fun testGenerateRandomPlot_CompositionCppBackend() {
        val width = 100
        val height = 100
        val fakeImageBytes = ByteArray(width * height * 4)
        
        every { 
            FractalRenderer.renderComposition(any(), any(), any(), any()) 
        } returns fakeImageBytes

        visualizeModel.selectedFigure = Figures.SOFT
        visualizeModel.randomSeed = 999L

        val result = generateRandomPlot(visualizeModel, width, height)

        assertNotNull(result)
        verify { 
            FractalRenderer.renderComposition(width, height, any(), any()) 
        }
    }
}
