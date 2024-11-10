package com.alejandro.randomplots.tools

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
