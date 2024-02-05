package com.example.randomplots.create

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.chaquo.python.Python
import java.util.Base64


fun generateRandomPlot(): Pair<ImageBitmap?, String>{
    val py = Python.getInstance()
    val mainModule = py.getModule("main")
    val result = mainModule.callAttr("generate", true).asList()

    val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())

    return Pair(
        BitmapFactory
        .decodeByteArray(imageBytes, 0, imageBytes.size)
        ?.asImageBitmap(),
        result[1].toString()
    )
}


//fun generateRandomPlot(){
//    val py = Python.getInstance()
//    val mainModule = py.getModule("main")
//    val result = mainModule.callAttr("generate", darkModeBool).asList()
//
//    val imageBytes = Base64.getDecoder().decode(result[0].toString().toByteArray())
//
//    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//    cachedBitmap = bitmap
//    imageView.setImageBitmap(bitmap)
//    latexView.setLatex(result[1].toString())
//    imageUri = saveImageToInternalStorage(bitmap, this)
//}