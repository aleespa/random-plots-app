package com.aleespa.randomsquare.data

import android.content.Context

class ImageRepository(
    private val imageDao: ImageDao,
    private val context: Context // Application context
) {
    suspend fun getFilteredImages(isDarkMode: Boolean?, imageType: String?): List<ImageEntity> {
        return imageDao.getFilteredImages(isDarkMode, imageType)
    }

    suspend fun insertImage(imageEntity: ImageEntity): Long {
        return imageDao.insertImage(imageEntity)
    }

    suspend fun deleteImageById(imageId: Int) {
        imageDao.deleteImageById(imageId)
    }

    suspend fun deleteAllImages() {
        imageDao.deleteAll()
    }

    suspend fun getImageCountsByType(): List<ImageTypeCount> {
        return imageDao.getImageCountsByType()
    }


}