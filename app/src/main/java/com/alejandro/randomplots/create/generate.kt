package com.alejandro.randomplots.create

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64


suspend fun generateRandomPlot(isDarkMode: Boolean): Pair<ImageBitmap?, String> = withContext(Dispatchers.IO) {
    val py = Python.getInstance()
    val mainModule = py.getModule("main")
    val result = mainModule.callAttr("generate", isDarkMode).asList()

    val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

    return@withContext Pair(
        BitmapFactory
            .decodeByteArray(imageBytes, 0, imageBytes.size)
            ?.asImageBitmap(),
        result[1].toString()
    )
}