package com.aleespa.randomsquare.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class ImageRepositoryTest {

    private lateinit var repository: ImageRepository
    private val imageDao: ImageDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val contentResolver: ContentResolver = mockk(relaxed = true)

    @Before
    fun setup() {
        every { context.contentResolver } returns contentResolver
        repository = ImageRepository(imageDao, context)
    }

    @Test
    fun getFilteredImages_shouldFilterOutInvalidUrisAndDeleteThemFromDao() {
        runBlocking {
            val validFile = File.createTempFile("valid", ".png")
            val validUri = "file://${validFile.absolutePath.replace("\\", "/")}"
            val invalidUri = "file:///non/existent/path.png"
            val contentUri = "content://com.aleespa.provider/image.png"
            
            val images = listOf(
                ImageEntity(id = 1, uri = validUri),
                ImageEntity(id = 2, uri = invalidUri),
                ImageEntity(id = 3, uri = contentUri)
            )
            
            coEvery { imageDao.getFilteredImages(any(), any()) } returns images
            
            // Mock content resolver for the content URI
            val inputStream: InputStream = mockk()
            every { contentResolver.openInputStream(Uri.parse(contentUri)) } returns inputStream
            every { inputStream.close() } returns Unit

            val result = repository.getFilteredImages(null, null)
            
            assertEquals("Should have 2 valid images", 2, result.size)
            assertEquals(1, result[0].id)
            assertEquals(3, result[1].id)
            
            coVerify { imageDao.deleteImages(listOf(images[1])) }
            
            validFile.delete()
        }
    }

    @Test
    fun insertImage_shouldCallDao() = runBlocking {
        val image = ImageEntity(uri = "file:///path.png")
        coEvery { imageDao.insertImage(image) } returns 1L
        
        val id = repository.insertImage(image)
        
        assertEquals(1L, id)
        coVerify { imageDao.insertImage(image) }
    }

    @Test
    fun deleteImageById_shouldCallDao() = runBlocking {
        repository.deleteImageById(1)
        coVerify { imageDao.deleteImageById(1) }
    }
}
