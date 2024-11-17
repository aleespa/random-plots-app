package com.alejandro.randomplots.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.alejandro.randomplots.Figures

class VisualizeModel: ViewModel() {
    var loading by mutableStateOf(false)
    var isRotated by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var selectedOption by mutableStateOf(Figures.SPIROGRAPH)
}