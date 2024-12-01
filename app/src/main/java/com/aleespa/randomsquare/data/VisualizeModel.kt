package com.aleespa.randomsquare.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleespa.randomsquare.Figures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.aleespa.randomsquare.data.ImageEntity.Builder

class VisualizeModel(private val dao: ImageDao): ViewModel() {
    var loadingPlotGenerator by mutableStateOf(false)
    var showInfo by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var selectedFigure by mutableStateOf(Figures.POLYGON_FEEDBACK)
    var isFromGallery by mutableStateOf(false)
    var galleryURI by mutableStateOf("")
    var galleryId by mutableIntStateOf(0)
    var darkFilter by mutableStateOf(false)
    var lightFilter by mutableStateOf(false)
    var filterImageType by mutableStateOf("None")
    var bgColor by mutableStateOf(Color(0,0,0,0))
    var isDarkMode by mutableStateOf(true)
    var randomSeed by mutableStateOf(0L)
    var isSavingLoading by mutableStateOf(false)
    var temporalImageEntity by mutableStateOf<Builder>(Builder())
    var toFitAspectRatio by mutableStateOf(false)

    var showFilterDialog by mutableStateOf(false)
    var showExitDialog by mutableStateOf(false)
    var showColorDialog by mutableStateOf(false)
    var showAspectRatioDialog by mutableStateOf(false)

    fun deleteImageById(imageId: Int) {
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

    suspend fun addImage(imageEntity: ImageEntity) {
        val id = withContext(Dispatchers.IO) {
            insertImage(imageEntity) // Call suspend function on IO dispatcher
        }
        galleryId = id.toInt() // This will be executed on the main thread
    }
}

data class FilterConditions(
    val isDarkMode: Boolean?,
    val imageType: String?
)

