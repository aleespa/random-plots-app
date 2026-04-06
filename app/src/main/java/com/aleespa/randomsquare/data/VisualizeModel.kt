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
import com.aleespa.randomsquare.Colormaps
import com.aleespa.randomsquare.FigureType
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
) : ViewModel() {
    var loadingPlotGenerator by mutableStateOf(false)
    var showInfo by mutableStateOf(false)
    var imageBitmapState by mutableStateOf<ImageBitmap?>(null)
    var latexString by mutableStateOf("")
    var newtonLatexString by mutableStateOf("")
    var isFromGallery by mutableStateOf(false)
    var galleryURI by mutableStateOf("")
    var galleryId by mutableIntStateOf(0)
    var darkFilter by mutableStateOf(false)
    var lightFilter by mutableStateOf(false)
    var filterImageType by mutableStateOf("None")
    var randomSeed by mutableLongStateOf(0L)
    var userSeed by mutableStateOf(false)
    var isSavingLoading by mutableStateOf(false)
    var temporalImageEntity by mutableStateOf<Builder>(Builder())
    var toFitAspectRatio by mutableStateOf(false)
    var showFilterDialog by mutableStateOf(false)
    var showAspectRatioDialog by mutableStateOf(false)

    var fractalZoom by mutableStateOf(1.0)
    var fractalXCenter by mutableStateOf(0.0)
    var fractalYCenter by mutableStateOf(0.0)
    var fractalIterations by mutableIntStateOf(150)
    var juliaCX by mutableStateOf(-0.7)
    var juliaCY by mutableStateOf(0.27015)

    var juliaR by mutableStateOf(kotlin.math.sqrt(juliaCX * juliaCX + juliaCY * juliaCY))
    var juliaTheta by mutableStateOf(kotlin.math.atan2(juliaCY, juliaCX))

    var multibrotD by mutableStateOf(3.0)
    var newtonCoeffs by mutableStateOf(DoubleArray(9) { 0.0 })

    fun updateJuliaFromPolar() {
        juliaCX = juliaR * kotlin.math.cos(juliaTheta)
        juliaCY = juliaR * kotlin.math.sin(juliaTheta)
    }

    fun updatePolarFromJulia() {
        juliaR = kotlin.math.sqrt(juliaCX * juliaCX + juliaCY * juliaCY)
        juliaTheta = kotlin.math.atan2(juliaCY, juliaCX)
    }

    var selectedColormap by mutableStateOf(Colormaps.RAINBOW)
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
            val wasFractal = _selectedFigure.figureType == FigureType.FRACTAL
            val isNowFractal = value.figureType == FigureType.FRACTAL
            val figureChanged = _selectedFigure != value
            _selectedFigure = value

            // Reset position and settings if switching to a fractal or between fractals
            if (isNowFractal && figureChanged) {
                resetFractalSettings()
            }

            // Update colormap if switching between fractal and non-fractal types
            if (isNowFractal && !wasFractal && !selectedColormap.isFractalSpecific) {
                selectedColormap = Colormaps.PLASMA_FRACTAL
            } else if (!isNowFractal && wasFractal && selectedColormap.isFractalSpecific) {
                selectedColormap = Colormaps.RAINBOW
            }

            viewModelScope.launch {
                settingsRepository.saveSelectedFigure(value) // Persist to DataStore
            }
        }

    init {
        // Load selectedFigure from DataStore
        viewModelScope.launch {
            settingsRepository.selectedFigure.collect { figure ->
                _selectedFigure = figure

                // Initialize fractal settings if starting with a fractal
                if (figure.figureType == FigureType.FRACTAL) {
                    resetFractalSettings()
                }

                // Ensure initial colormap matches the figure type
                if (figure.figureType == FigureType.FRACTAL && !selectedColormap.isFractalSpecific) {
                    selectedColormap = Colormaps.PLASMA_FRACTAL
                } else if (figure.figureType != FigureType.FRACTAL && selectedColormap.isFractalSpecific) {
                    selectedColormap = Colormaps.RAINBOW
                }
            }
        }
    }

    private var _selectedColormapColors by mutableStateOf(
        listOf(
            0xFFFF0000.toInt(), // red
            0xFF0000FF.toInt(), // blue
            0xFFFFFF00.toInt(), // yellow
            0xFF00FFFF.toInt()  // cyan
        )
    )

    var selectedColormapColors: List<Int>
        get() = _selectedColormapColors
        set(value) {
            _selectedColormapColors = value
            viewModelScope.launch {
                settingsRepository.saveSelectedColormapColors(value) // Persist to DataStore
            }
        }

    init {
        // Load selectedColormapColors from DataStore
        viewModelScope.launch {
            settingsRepository.selectedColormapColors.collect { colors ->
                _selectedColormapColors = colors
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

    fun resetFractalSettings() {
        fractalZoom = 1.0
        fractalXCenter = if (selectedFigure == Figures.MANDELBROT) -1.0 else 0.0
        fractalYCenter = 0.0
        fractalIterations = when (selectedFigure) {
            Figures.MANDELBROT -> 100
            Figures.JULIA -> 80
            Figures.TRICORN -> 80
            Figures.MULTIBROT -> 100
            Figures.NEWTON -> 100
            else -> 150
        }
        if (selectedFigure == Figures.NEWTON) {
            newtonCoeffs = doubleArrayOf(-1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0) // z^3 - 1
        }
        juliaCX = -0.7
        juliaCY = 0.27015
        updatePolarFromJulia()
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


