package com.aleespa.randomsquare.tools

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aleespa.randomsquare.data.VisualizeModel
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView
import java.io.IOException


@Composable
fun LatexMathView(visualizeModel: VisualizeModel) {
    var textColor = if (visualizeModel.bgColor.type == "Dark") Color.WHITE else Color.BLACK
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(10.dp),
        factory = { context ->
            JLatexMathView(context).apply {
                visibility = View.VISIBLE
            }
        },
        update = { view ->
            val drawable = JLatexMathDrawable.builder(visualizeModel.latexString)
                .align(JLatexMathDrawable.ALIGN_CENTER)
                .textSize(70F)
                .padding(8)
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