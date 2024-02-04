package com.example.randomplots.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.randomplots.R
import com.example.randomplots.create.generateRandomPlot
import org.scilab.forge.jlatexmath.TeXFormula
import ru.noties.jlatexmath.JLatexMathView
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Create() {
    var rotated by remember {
        mutableStateOf(false)
    }
    val imageBitmapState = remember { mutableStateOf<ImageBitmap?>(null) }

    Column (
        modifier = Modifier
            .fillMaxSize(), // Adjust padding as needed
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(Modifier.width(30.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { rotated = !rotated },
        ) {
            if (!rotated){
                ImageWithNullFallback(imageBitmapState.value)
            } else {
                LaTeXMathView(
                    latex="This is a test",
                    backgroundColor = MaterialTheme.colorScheme.surface.toString(),
                    fontColor = MaterialTheme.colorScheme.primary.toString()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This is a test",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }
        Column {
            ElevatedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = { imageBitmapState.value = generateRandomPlot() }) {
                Text(
                    text = "Generate\nRandom Plot",
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                ExtendedFloatingActionButton(onClick = { /* do something */ }) {
                    Text(text = "Save to\ngallery")
                }
                Spacer(Modifier.width(10.dp))
                ExtendedFloatingActionButton(onClick = { /* do something */ }) {
                    Text(
                        text = "Set as\n wallpaper",
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(90.dp))
        }

    }
}
@Composable
fun ImageWithNullFallback(imageBitmap: ImageBitmap?) {
    val painter = if (imageBitmap != null) {
        BitmapPainter(imageBitmap)
    } else {
        painterResource(id = R.drawable.cover_random)
    }

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Inside
    )
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LaTeXMathView(latex: String, backgroundColor: String, fontColor: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            loadDataWithBaseURL(
                null, getHTML(latex, backgroundColor, fontColor),
                "text/html",
                "UTF-8",
                null)
            webViewClient = WebViewClient()
        }
    })
}

private fun getHTML(latex: String, backgroundColor: String, fontColor: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    background-color: $backgroundColor;
                    color: $fontColor;
                }
            </style>
            <link rel="stylesheet" type="text/css" href="file:///android_asset/css/jlatexmath-core.css">
            <script src="file:///android_asset/js/jlatexmath.js"></script>
        </head>
        <body>
            <span id="mathField">$latex</span>
            <script type="text/javascript">
                var mathField = document.getElementById("mathField");
                var options = {
                    displayMode: true,
                    fontSize: 20,
                    latex: mathField.innerHTML
                };
                var latexNode = new Latex(options);
                mathField.parentNode.insertBefore(latexNode, mathField.nextSibling);
            </script>
        </body>
        </html>
    """.trimIndent()
}

@Preview
@Composable
fun GreetingPreview() {
    Create()
}
