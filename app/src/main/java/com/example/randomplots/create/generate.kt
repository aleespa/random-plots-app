package com.example.randomplots.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.chaquo.python.Python
import java.util.Base64


fun generateRandomPlot(): ImageBitmap? {
    val py = Python.getInstance()
    val mainModule = py.getModule("main")
    val result = mainModule.callAttr("generate", true).asList()

    val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
}