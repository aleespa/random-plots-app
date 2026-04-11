package com.aleespa.randomsquare.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aleespa.randomsquare.data.DatabaseProvider
import java.io.File

class PruneImagesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val imageDao = DatabaseProvider.getDatabase(applicationContext).imageDao()
        val images = imageDao.getAllImagesList()

        val invalidImages = images.filter { !isUriValid(it.uri) }
        
        if (invalidImages.isNotEmpty()) {
            imageDao.deleteImages(invalidImages)
        }

        return androidx.work.ListenableWorker.Result.success()
    }

    private fun isUriValid(uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString) ?: return false
            when (uri.scheme) {
                "file" -> {
                    val path = uri.path
                    if (path != null) File(path).exists() else false
                }
                "content" -> {
                    applicationContext.contentResolver.openInputStream(uri)?.use { true } ?: false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}