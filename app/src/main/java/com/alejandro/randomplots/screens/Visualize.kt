package com.alejandro.randomplots.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alejandro.randomplots.Figures
import com.alejandro.randomplots.R
import com.alejandro.randomplots.data.VisualizeModel
import com.alejandro.randomplots.tools.LatexMathView
import com.alejandro.randomplots.tools.generateRandomPlot
import com.alejandro.randomplots.tools.loadBitmapFromFile
import com.alejandro.randomplots.tools.saveBitmapToFile
import com.alejandro.randomplots.tools.saveBitmapToGallery
import com.alejandro.randomplots.tools.setWallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Visualize(visualizeModel: VisualizeModel = viewModel()) {
    var selectedOption = remember { mutableStateOf("") }
    val context = LocalContext.current
    val savedBitmap = loadBitmapFromFile(context, "cache_front.png")
    if (savedBitmap != null) {
        visualizeModel.imageBitmapState = savedBitmap.asImageBitmap()
    }
    val options = Figures.entries.map { it.s }
    Column (
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier= Modifier.height(50.dp))
//        SwitchWithIconExample()
        selectedOption.value = dropdownMenu(options)
        Spacer(Modifier.height(10.dp))
        VisualizeBox(visualizeModel)
        Spacer(Modifier.height(35.dp))
        VisualizeButtons(visualizeModel, context, selectedOption.value)
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
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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

@Composable
fun SwitchWithIconExample() {
    var checked by remember { mutableStateOf(true) }

    Switch(
        checked = checked,
        onCheckedChange = {
            checked = it
        },
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    )
}
@Composable
fun SetWallpaperButton(
    imageBitmapState: ImageBitmap?,
    context: Context) {
    ExtendedFloatingActionButton(
        elevation = FloatingActionButtonDefaults.elevation(10.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = {
            val androidBitmap = imageBitmapState?.asAndroidBitmap()
            if (androidBitmap != null) {
                setWallpaper(context, androidBitmap)
            }
            Log.d("", "Wallpaper set")
        }) {
        Text(
            text = stringResource(id = R.string.set_wallpaper),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SaveToGalleryButton(imageBitmapState: ImageBitmap?, context: Context) {
    ExtendedFloatingActionButton(
        elevation = FloatingActionButtonDefaults.elevation(10.dp),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = {
            val androidBitmap = imageBitmapState?.asAndroidBitmap()
            if (androidBitmap != null) {
                saveBitmapToGallery(context, androidBitmap)
            }
        }) {
        Text(
            text = stringResource(id = R.string.save),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GeneratePlotButton(
    visualizeModel: VisualizeModel,
    context: Context,
    isDarkTheme: Boolean,
    selectedOption:String,
    ){
    ExtendedFloatingActionButton(
        elevation = FloatingActionButtonDefaults.elevation(10.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = {

            CoroutineScope(Dispatchers.Main).launch {
                visualizeModel.loading = true
                delay(1)
                val result = generateRandomPlot(isDarkTheme, selectedOption)
                val androidBitmap = result.first?.asAndroidBitmap()
                if (androidBitmap != null) {
                    saveBitmapToFile(context, androidBitmap,
                        "cache_front.png")
                }
                visualizeModel.imageBitmapState = result.first
                visualizeModel.latexString = result.second
                visualizeModel.loading = false
            }}) {
        Text(
            text = stringResource(id = R.string.generate),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VisualizeButtons(
    visualizeModel: VisualizeModel,
    context: Context,
    selectedOption: String,
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        GeneratePlotButton(visualizeModel, context, isSystemInDarkTheme(), selectedOption)
        Spacer(Modifier.height(15.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            SaveToGalleryButton(visualizeModel.imageBitmapState, context)
            Spacer(Modifier.width(15.dp))
            SetWallpaperButton(visualizeModel.imageBitmapState, context)
        }
    }
}

@Composable
fun VisualizeBox(visualizeModel: VisualizeModel){
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(10.dp)
            .clickable { visualizeModel.isRotated = !visualizeModel.isRotated },
    ) {
        if (!visualizeModel.isRotated){
            if (visualizeModel.loading) {
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
                ImageWithNullFallback(visualizeModel.imageBitmapState)
            }

        }else{
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                LatexMathView(
                    latexString = visualizeModel.latexString
                )
            }

        }
    }
}