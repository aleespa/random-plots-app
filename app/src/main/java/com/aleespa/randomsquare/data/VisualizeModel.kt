package com.aleespa.randomsquare.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aleespa.randomsquare.Figures
import com.aleespa.randomsquare.data.ImageEntity.Builder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisualizeModelFactory(
    private val imageRepository: ImageRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisualizeModel(imageRepository, settingsRepository) as T
    }
}

class VisualizeModel(
    private val imageRepository: ImageRepository,
    private val settingsRepository: AppSettingsRepository
): ViewModel() {
    var loadingPlotGenerator by mutableStateOf(false)
    var showInfo by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var isFromGallery by mutableStateOf(false)
    var galleryURI by mutableStateOf("")
    var galleryId by mutableIntStateOf(0)
    var darkFilter by mutableStateOf(false)
    var lightFilter by mutableStateOf(false)
    var filterImageType by mutableStateOf("None")
    var randomSeed by mutableLongStateOf(0L)
    var isSavingLoading by mutableStateOf(false)
    var temporalImageEntity by mutableStateOf<Builder>(Builder())
    var toFitAspectRatio by mutableStateOf(false)
    var showFilterDialog by mutableStateOf(false)
    var showAspectRatioDialog by mutableStateOf(false)

    private var _settingDarkMode by mutableStateOf(SettingDarkMode.Auto)
    var settingDarkMode: SettingDarkMode
        get() = _settingDarkMode
        set(value) {
            _settingDarkMode = value
            viewModelScope.launch {
                settingsRepository.saveDarkModeSetting(value) // Persist to DataStore
            }
        }
    init {
        // Load settingDarkMode from DataStore
        viewModelScope.launch {
            settingsRepository.darkModeSetting.collect { mode ->
                _settingDarkMode = mode
            }
        }
    }

    private var _selectedFigure by mutableStateOf(Figures.POLYGON_FEEDBACK)
    var selectedFigure: Figures
        get() = _selectedFigure
        set(value) {
            _selectedFigure = value
            viewModelScope.launch {
                settingsRepository.saveSelectedFigure(value) // Persist to DataStore
            }
        }

    init {
        // Load selectedFigure from DataStore
        viewModelScope.launch {
            settingsRepository.selectedFigure.collect { figure ->
                _selectedFigure = figure
            }
        }
    }

    private var _bgColor by mutableStateOf(0)
    var bgColor: Int
        get() = _bgColor
        set(value) {
            _bgColor = value
            viewModelScope.launch {
                settingsRepository.saveBgColor(value) // Persist to DataStore
            }
        }

    init {
        viewModelScope.launch {
            settingsRepository.bgColor.collect { color ->
                _bgColor = color
            }
        }
    }

    suspend fun deleteImageById(imageId: Int) {
        return imageRepository.deleteImageById(imageId)
    }

    suspend fun insertImage(imageEntity: ImageEntity): Long {
        return imageRepository.insertImage(imageEntity)
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



    private suspend fun getFilteredImages(filterConditions: FilterConditions): List<ImageEntity> {
        return imageRepository.getFilteredImages(
            isDarkMode = filterConditions.isDarkMode,
            imageType = filterConditions.imageType
        )
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


