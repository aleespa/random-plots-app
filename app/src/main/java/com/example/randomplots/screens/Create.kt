package com.example.randomplots.screens

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
                    text = "Generate \n Random Plot",
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
                    Text(text = "More info")
                }
                Spacer(Modifier.width(10.dp))
                ExtendedFloatingActionButton(onClick = { /* do something */ }) {
                    Text(
                        text = "Set as \n wallpaper",
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


@Preview
@Composable
fun GreetingPreview() {
    Create()
}
