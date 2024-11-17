package com.alejandro.randomplots.tools

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView
import java.io.IOException


@Composable
fun LatexMathView(latexString: String) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(10.dp),
        factory = { context ->
            JLatexMathView(context).apply {
                visibility = View.VISIBLE
            }
        },
        update = { view ->
            val drawable = JLatexMathDrawable.builder(latexString)
                .textSize(70F)
                .padding(8)
                .align(JLatexMathDrawable.ALIGN_CENTER)
                .color(textColor)
                .build()
            view.setLatexDrawable(drawable)
        }
    )
}

fun readTexAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open("latex/${fileName}.tex").use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
    } catch (e: IOException) {
        Log.e("Asset Reading", "Error reading text file: ${e.message}")
        ""
    }
}