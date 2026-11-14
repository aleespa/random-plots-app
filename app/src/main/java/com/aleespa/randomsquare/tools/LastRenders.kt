package com.aleespa.randomsquare.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateMapOf
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.ImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * The last image rendered for each figure, with the settings that produced it. Browse shows it
 * as the figure's card and tapping the card reopens it. Stored in `files/last/` as
 * `<key>.webp` (a downscaled preview; reopening re-renders at full quality) and `<key>.json`
 * (an [ImageEntity] without id/uri, the same record the gallery restores from).
 */
object LastRenders {
    /** Longest side of the stored preview. Enough for a Browse card and the instant on reopen. */
    private const val PREVIEW_SIZE = 720

    /**
     * Figure key -> preview file timestamp, for every figure that has one. Compose state, so
     * Browse cards switch to a new preview as soon as it is written.
     */
    val stamps = mutableStateMapOf<String, Long>()

    private var loaded = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val pending = ConcurrentHashMap<String, Long>()

    private fun dir(context: Context) = File(context.filesDir, "last")
    private fun imageFile(context: Context, key: String) = File(dir(context), "$key.webp")
    private fun stateFile(context: Context, key: String) = File(dir(context), "$key.json")

    /** Reads which figures have a preview (a directory listing; cheap enough for the main thread). */
    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        dir(context).listFiles()?.forEach { f ->
            if (f.extension == "webp" && stateFile(context, f.nameWithoutExtension).exists()) {
                stamps[f.nameWithoutExtension] = f.lastModified()
            }
        }
    }

    /** The preview file for [figure], or null if it has never been rendered. */
    fun previewFile(context: Context, figure: Figures): File? {
        ensureLoaded(context)
        return if (stamps.containsKey(figure.key)) imageFile(context, figure.key) else null
    }

    /**
     * Stores [bitmap] as [entity]'s figure's last render, in the background. Only the newest
     * request per figure is written, so a burst of pan/zoom renders costs one write.
     */
    fun save(context: Context, entity: ImageEntity, bitmap: Bitmap) {
        val app = context.applicationContext
        val key = entity.imageType
        val token = System.nanoTime()
        pending[key] = token
        scope.launch {
            if (pending[key] != token) return@launch
            val scale = PREVIEW_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
            val preview = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                )
            } else bitmap
            dir(app).mkdirs()
            // Write to temporary files and rename, so a reader never sees half a file.
            val img = imageFile(app, key)
            val json = stateFile(app, key)
            val tmpImg = File(img.path + ".tmp")
            val tmpJson = File(json.path + ".tmp")
            tmpImg.outputStream().use { preview.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, it) }
            tmpJson.writeText(entity.toJson().toString())
            tmpImg.renameTo(img)
            tmpJson.renameTo(json)
            val stamp = img.lastModified()
            withContext(Dispatchers.Main) { stamps[key] = stamp }
        }
    }

    /** The last render of [figure]: its settings and preview, or null if there is none. */
    suspend fun load(context: Context, figure: Figures): Pair<ImageEntity, Bitmap>? =
        withContext(Dispatchers.IO) {
            val img = imageFile(context, figure.key)
            val json = stateFile(context, figure.key)
            if (!img.exists() || !json.exists()) return@withContext null
            runCatching {
                val entity = imageEntityFromJson(JSONObject(json.readText()))
                val bitmap = BitmapFactory.decodeFile(img.path) ?: return@runCatching null
                entity to bitmap
            }.getOrNull()
        }

    /** Deletes every stored render; Browse goes back to the bundled samples. */
    fun clear(context: Context) {
        stamps.clear()
        pending.clear()
        val app = context.applicationContext
        scope.launch { dir(app).deleteRecursively() }
    }
}

internal fun ImageEntity.toJson(): JSONObject = JSONObject().apply {
    put("imageType", imageType)
    put("timestamp", timestamp)
    put("isDarkMode", isDarkMode)
    put("backgroundColor", backgroundColor)
    randomSeed?.let { put("randomSeed", it) }
    fractalXCenter?.let { put("fractalXCenter", it) }
    fractalYCenter?.let { put("fractalYCenter", it) }
    fractalZoom?.let { put("fractalZoom", it) }
    listOf(param1, param2, param3, param4, param5, param6, param7, param8, param9, param10)
        .forEachIndexed { i, v -> v?.let { put("param${i + 1}", it) } }
    colormap?.let { put("colormap", it) }
    iterations?.let { put("iterations", it) }
}

internal fun imageEntityFromJson(o: JSONObject): ImageEntity {
    fun d(name: String) = if (o.has(name)) o.getDouble(name) else null
    return ImageEntity(
        imageType = o.getString("imageType"),
        timestamp = o.optLong("timestamp"),
        isDarkMode = o.optBoolean("isDarkMode"),
        backgroundColor = o.optInt("backgroundColor"),
        randomSeed = if (o.has("randomSeed")) o.getLong("randomSeed") else null,
        fractalXCenter = d("fractalXCenter"),
        fractalYCenter = d("fractalYCenter"),
        fractalZoom = d("fractalZoom"),
        param1 = d("param1"), param2 = d("param2"), param3 = d("param3"),
        param4 = d("param4"), param5 = d("param5"), param6 = d("param6"),
        param7 = d("param7"), param8 = d("param8"), param9 = d("param9"),
        param10 = d("param10"),
        colormap = if (o.has("colormap")) o.getString("colormap") else null,
        iterations = if (o.has("iterations")) o.getInt("iterations") else null,
    )
}
