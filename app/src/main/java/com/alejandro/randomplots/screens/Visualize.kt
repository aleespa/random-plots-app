package com.alejandro.randomplots.screens

import android.util.Log
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alejandro.randomplots.Figures
import com.alejandro.randomplots.R
import com.alejandro.randomplots.tools.generateRandomPlot
import com.alejandro.randomplots.tools.loadBitmapFromFile
import com.alejandro.randomplots.tools.saveBitmapToFile
import com.alejandro.randomplots.tools.saveBitmapToGallery
import com.alejandro.randomplots.tools.setWallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.noties.jlatexmath.JLatexMathDrawable
import ru.noties.jlatexmath.JLatexMathView

@Composable
fun Visualize() {
    Log.d("","Create ")
    val isDarkTheme = isSystemInDarkTheme()
    var rotated by remember {
        mutableStateOf(false)
    }
    var loading by remember {
        mutableStateOf(false)
    }

    val imageBitmapState = remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val latexString = remember {
        mutableStateOf<String>("")
    }
    val context = LocalContext.current
    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if (savedBitmap != null) {
        imageBitmapState.value = savedBitmap.asImageBitmap()
    }
    val options = Figures.entries.map { it.s }
    Column (
        modifier = Modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(50.dp))
        val selectedOption = dropdownMenu(options)
        Spacer(Modifier.height(10.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { rotated = !rotated },
        ) {
            if (!rotated){
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                } else{
                    ImageWithNullFallback(imageBitmapState.value)
                }

            }else{
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    LatexMathView(
                        latexString = latexString.value
                    )
                }

            }
        }
        Spacer(Modifier.height(50.dp))
        Column {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                elevation = FloatingActionButtonDefaults.elevation(10.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    loading = true
                    // Introduce a delay before starting the long-running operation
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1) // Adjust delay time as needed
                        val result = generateRandomPlot(isDarkTheme, selectedOption)
                        val androidBitmap = result.first?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            saveBitmapToFile(context, androidBitmap,
                                "cache_front.png")
                        }
                        imageBitmapState.value = result.first
                        latexString.value = result.second
                        loading = false
                    }}) {
                Text(
                    text = stringResource(id = R.string.generate),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(15.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                ExtendedFloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(10.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val androidBitmap = imageBitmapState.value?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            saveBitmapToGallery(context, androidBitmap)
                    }
                }) {
                    Text(
                        text = stringResource(id = R.string.save),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.width(15.dp))
                ExtendedFloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(10.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val androidBitmap = imageBitmapState.value?.asAndroidBitmap()
                        if (androidBitmap != null) {
                            setWallpaper(context, androidBitmap)
                        }
                        Log.d("","Wallpaper set")
                }) {
                    Text(
                        text = stringResource(id = R.string.set_wallpaper),
                        textAlign = TextAlign.Center,
                    )
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropdownMenu(options: List<String>): String {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    return Figures.fromCode(selectedOption)?.s1 ?: ""
}
