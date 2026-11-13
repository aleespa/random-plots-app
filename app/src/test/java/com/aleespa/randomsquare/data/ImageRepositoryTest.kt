package com.aleespa.randomsquare.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.aleespa.randomsquare.workers.isImageUriValid
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    /** Filtering is a plain DAO query; invalid entries are pruned by PruneImagesWorker instead. */
    @Test
    fun getFilteredImages_returnsDaoResultsWithoutDeleting() = runBlocking {
        val images = listOf(
            ImageEntity(id = 1, uri = "file:///non/existent/path.png"),
            ImageEntity(id = 2, uri = "content://com.aleespa.provider/image.png")
        )
        coEvery { imageDao.getFilteredImages(true, "spiral") } returns images

        assertEquals(images, repository.getFilteredImages(true, "spiral"))
        coVerify(exactly = 0) { imageDao.deleteImages(any()) }
    }

    @Test
    fun isImageUriValid_keepsExistingFilesAndOpenableContent_rejectsTheRest() {
        val validFile = File.createTempFile("valid", ".png")
        try {
            val contentUri = "content://com.aleespa.provider/image.png"
            val inputStream: InputStream = mockk(relaxed = true)
            every { contentResolver.openInputStream(Uri.parse(contentUri)) } returns inputStream
            every { contentResolver.openInputStream(Uri.parse("content://gone/image.png")) } returns null

            assertTrue(isImageUriValid(contentResolver, validFile.toURI().toString()))
            assertTrue(isImageUriValid(contentResolver, contentUri))
            assertFalse(isImageUriValid(contentResolver, "file:///non/existent/path.png"))
            assertFalse(isImageUriValid(contentResolver, "content://gone/image.png"))
            assertFalse(isImageUriValid(contentResolver, "not a uri"))
        } finally {
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
