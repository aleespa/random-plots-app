package com.aleespa.randomsquare.tools

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView
import java.io.IOException
import android.graphics.Color as AndroidColor

@Composable
fun LatexMathView(latex: String, bgColor: Int) {
    val textColor = if (isColorDark(bgColor)) AndroidColor.WHITE else AndroidColor.BLACK
    val latexDisplay = latex.ifEmpty { " " }
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        factory = { context ->
            JLatexMathView(context)
        },
        update = { view ->
            try {
                val drawable = JLatexMathDrawable.builder(latexDisplay)
                    .textSize(70F)
                    .padding(8)
                    .color(textColor)
                    .build()
                view.setLatexDrawable(drawable)
            } catch (e: Exception) {
                Log.e("LatexMathView", "Error rendering LaTeX: ${e.message}")
            }
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

fun generateNewtonLatex(coeffs: DoubleArray): String {
    val sb = StringBuilder("p(z) = ")
    var first = true
    for (i in coeffs.indices.reversed()) {
        val c = coeffs[i].toInt()
        if (c == 0) continue

        val absC = kotlin.math.abs(c)

        if (first) {
            if (c < 0) sb.append("-")
        } else {
            sb.append(if (c > 0) " + " else " - ")
        }

        val coeffStr = if (absC == 1 && i > 0) "" else absC.toString()
        val zPart = when (i) {
            0 -> "" // Handled by appending absC below
            1 -> "z"
            else -> "z^{$i}"
        }

        if (i == 0) {
            sb.append(absC)
        } else {
            sb.append(coeffStr).append(zPart)
        }

        first = false
    }
    if (first) return "p(z) = 0"
    return sb.toString()
}