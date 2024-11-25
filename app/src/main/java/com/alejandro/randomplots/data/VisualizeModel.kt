package com.alejandro.randomplots.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.randomplots.Figures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisualizeModel(private val dao: ImageDao): ViewModel() {
    var loading by mutableStateOf(false)
    var isRotated by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var selectedOption by mutableStateOf(Figures.SPIROGRAPH)
    var isDarkMode by mutableStateOf(false)
    var isFromGallery by mutableStateOf(false)
    var galleryURI by mutableStateOf("")
    var galleryId by mutableIntStateOf(0)

    var darkFilter by mutableStateOf(false)
    var lightFilter by mutableStateOf(false)

    var showFilterDialog by mutableStateOf(false)
    var filterImageType by mutableStateOf("None") // Selected filter option


    var showColorDialog by mutableStateOf(false)
    var bgColor by mutableStateOf(Color(0,0,0,0))

    private val _images = dao.getAllImages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Active for 5 seconds after UI stops collecting
            initialValue = emptyList()
        )
    val images: StateFlow<List<ImageEntity>> = _images

    private val _darkImages = dao.getImageByIsDarkMode(true)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val darkImages: StateFlow<List<ImageEntity>> = _darkImages

    private val _lightImages = dao.getImageByIsDarkMode(false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val lightImages: StateFlow<List<ImageEntity>> = _lightImages

    suspend fun deleteImageById(imageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteImageById(imageId)
        }
    }

    suspend fun insertImage(imageEntity: ImageEntity): Long {
        return dao.insertImage(imageEntity)
    }

    fun getImageFilterConditions(): FilterConditions {
        // Default conditions, selecting all
        var isDarkMode: Boolean? = null
        var imageType: String? = null

        // Handling the darkMode filter
        if (darkFilter) {
            isDarkMode = true
        } else if (lightFilter) {
            isDarkMode = false
        }

        if (filterImageType != "None") {
            imageType = filterImageType
        }

        return FilterConditions(isDarkMode, imageType)
    }
    suspend fun getFilteredImages(filterConditions: FilterConditions): List<ImageEntity> {
        return dao.getFilteredImages(filterConditions.isDarkMode, filterConditions.imageType)
    }
    // MutableStateFlow to hold filtered images
    private val _filteredImages = MutableStateFlow<List<ImageEntity>>(emptyList())
    val filteredImages: StateFlow<List<ImageEntity>> = _filteredImages

    // Method to update the filtered images based on user selections
    fun updateFilteredImages() {
        val filterConditions = getImageFilterConditions()
        // Update the filtered images using the repository method
        viewModelScope.launch {
            _filteredImages.value = getFilteredImages(filterConditions)
        }
    }

    fun addImage(imageEntity: ImageEntity) {
        viewModelScope.launch {
            val id = insertImage(imageEntity) // Call suspend function
            withContext(Dispatchers.Main) { // Update the UI-related property on the main thread
                galleryId = id.toInt() // Assign the result
            }
        }
    }
}

data class FilterConditions(
    val isDarkMode: Boolean?,
    val imageType: String?
)

