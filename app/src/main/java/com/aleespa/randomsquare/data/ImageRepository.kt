package com.aleespa.randomsquare.data

import android.content.Context
import android.net.Uri
import java.io.File

class ImageRepository(
    private val imageDao: ImageDao,
    private val context: Context // Application context
) {
    private fun isUriValid(uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            when (uri.scheme) {
                "file" -> File(uri.path).exists()
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { true } ?: false
                }

                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFilteredImages(isDarkMode: Boolean?, imageType: String?): List<ImageEntity> {
        val images = imageDao.getFilteredImages(isDarkMode, imageType)
        val (validImages, invalidImages) = images.partition { isUriValid(it.uri) }
        if (invalidImages.isNotEmpty()) {
            imageDao.deleteImages(invalidImages)
        }
        return validImages
    }

    suspend fun insertImage(imageEntity: ImageEntity): Long {
        return imageDao.insertImage(imageEntity)
    }

    suspend fun deleteImageById(imageId: Int) {
        imageDao.deleteImageById(imageId)
    }


}