package com.aleespa.randomsquare.workers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aleespa.randomsquare.data.DatabaseProvider
import java.io.File

/** Removes gallery entries whose image file no longer exists (e.g. deleted outside the app). */
class PruneImagesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val imageDao = DatabaseProvider.getDatabase(applicationContext).imageDao()
        val images = imageDao.getAllImagesList()

        val resolver = applicationContext.contentResolver
        val invalidImages = images.filter { !isImageUriValid(resolver, it.uri) }

        if (invalidImages.isNotEmpty()) {
            imageDao.deleteImages(invalidImages)
        }

        return Result.success()
    }
}

/**
 * Whether a stored image URI still points at a readable image: `file://` URIs must exist,
 * `content://` URIs must open. Anything else (or a malformed URI) is invalid.
 */
internal fun isImageUriValid(contentResolver: ContentResolver, uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString) ?: return false
        when (uri.scheme) {
            "file" -> uri.path?.let { File(it).exists() } ?: false
            "content" -> contentResolver.openInputStream(uri)?.use { true } ?: false
            else -> false
        }
    } catch (e: Exception) {
        false
    }
}
