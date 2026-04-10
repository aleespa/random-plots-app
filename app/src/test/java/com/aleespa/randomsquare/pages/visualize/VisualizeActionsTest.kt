package com.aleespa.randomsquare.pages.visualize

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.AppSettingsRepository
import com.aleespa.randomsquare.data.ImageEntity
import com.aleespa.randomsquare.data.ImageRepository
import com.aleespa.randomsquare.data.SettingDarkMode
import com.aleespa.randomsquare.data.VisualizeModel
import com.aleespa.randomsquare.tools.saveBitmapToGallery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class VisualizeActionsTest {

    private val context: Context = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val settingsRepository: AppSettingsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var visualizeModel: VisualizeModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { settingsRepository.darkModeSetting } returns kotlinx.coroutines.flow.flowOf(SettingDarkMode.Auto)
        every { settingsRepository.selectedFigure } returns kotlinx.coroutines.flow.flowOf(Figures.POLYGON_FEEDBACK)
        every { settingsRepository.selectedColormapColors } returns kotlinx.coroutines.flow.flowOf(emptyList())
        every { settingsRepository.bgColor } returns kotlinx.coroutines.flow.flowOf(0)

        visualizeModel = VisualizeModel(imageRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testDeleteFromGallery() = runTest {
        visualizeModel.galleryId = 123
        visualizeModel.galleryURI = "content://media/external/images/media/123"
        visualizeModel.isFromGallery = true

        deleteFromGallery(visualizeModel, context)
        advanceUntilIdle()

        coVerify { imageRepository.deleteImageById(123) }
        assertEquals("", visualizeModel.galleryURI)
        assertEquals(0, visualizeModel.galleryId)
        assertFalse(visualizeModel.isFromGallery)
    }

    @Test
    fun testSaveToGallery() = runTest {
        mockkStatic("com.aleespa.randomsquare.tools.GenerateKt")
        mockkStatic(BitmapFactory::class)
        
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        visualizeModel.imageBitmapState = bitmap.asImageBitmap()
        visualizeModel.selectedFigure = Figures.SPIROGRAPH
        
        val fakeUri = Uri.parse("content://media/external/images/media/456")
        every { saveBitmapToGallery(any(), any(), any()) } returns fakeUri
        every { BitmapFactory.decodeByteArray(any(), any(), any()) } returns bitmap

        saveToGallery(visualizeModel, context)
        advanceUntilIdle()

        coVerify { imageRepository.insertImage(any()) }
        assertEquals(fakeUri.toString(), visualizeModel.galleryURI)
        assertEquals(true, visualizeModel.isFromGallery)
        assertEquals(false, visualizeModel.isSavingLoading)
    }
}
