package com.alejandro.randomplots.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.randomplots.Figures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VisualizeModel(private val dao: ImageDao): ViewModel() {
    var loading by mutableStateOf(false)
    var isRotated by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var selectedOption by mutableStateOf(Figures.SPIROGRAPH)
    var isDarkMode by mutableStateOf(false)
    var isFromGallery by mutableStateOf(false)
    var galleryURI by mutableStateOf("")
    var galleryId by mutableStateOf(0)

    private val _images = MutableStateFlow<List<ImageEntity>>(emptyList())
    val images: StateFlow<List<ImageEntity>> = _images

    fun fetchImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val imageList = dao.getAllImages()
            _images.value = imageList
        }
    }

    fun deleteImageById(imageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteImageById(imageId)
            fetchImages() // Refresh the list after deletion
        }
    }

}