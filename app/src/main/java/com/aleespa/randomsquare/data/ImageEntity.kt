package com.aleespa.randomsquare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String = "",
    val imageType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDarkMode: Boolean = false,
    val randomSeed: Long? = 0,
    val backgroundColor: Int = 0,
    val fractalXCenter: Double? = null,
    val fractalYCenter: Double? = null,
    val fractalZoom: Double? = null,
    val juliaCX: Double? = null,
    val juliaCY: Double? = null,
    val colormap: String? = null,
    val fractalIterations: Int? = null
) {
    // Builder class
    class Builder {
        private var uri: String = ""
        private var imageType: String = ""
        private var timestamp: Long = System.currentTimeMillis()
        private var isDarkMode: Boolean = false
        private var randomSeed: Long? = 0
        private var backgroundColor: Int = 0
        private var fractalXCenter: Double? = null
        private var fractalYCenter: Double? = null
        private var fractalZoom: Double? = null
        private var juliaCX: Double? = null
        private var juliaCY: Double? = null
        private var colormap: String? = null
        private var fractalIterations: Int? = null

        fun setUri(uri: String) = apply { this.uri = uri }
        fun setImageType(imageType: String) = apply { this.imageType = imageType }
        fun setTimestamp(timestamp: Long) = apply { this.timestamp = timestamp }
        fun setIsDarkMode(isDarkMode: Boolean) = apply { this.isDarkMode = isDarkMode }
        fun setRandomSeed(randomSeed: Long?) = apply { this.randomSeed = randomSeed }
        fun setBackgroundColor(backgroundColor: Int) =
            apply { this.backgroundColor = backgroundColor }

        fun setFractalXCenter(x: Double?) = apply { this.fractalXCenter = x }
        fun setFractalYCenter(y: Double?) = apply { this.fractalYCenter = y }
        fun setFractalZoom(z: Double?) = apply { this.fractalZoom = z }
        fun setJuliaCX(cx: Double?) = apply { this.juliaCX = cx }
        fun setJuliaCY(cy: Double?) = apply { this.juliaCY = cy }
        fun setColormap(colormap: String?) = apply { this.colormap = colormap }
        fun setFractalIterations(iterations: Int?) = apply { this.fractalIterations = iterations }

        fun build(): ImageEntity {
            return ImageEntity(
                uri = uri,
                imageType = imageType,
                timestamp = timestamp,
                isDarkMode = isDarkMode,
                randomSeed = randomSeed,
                backgroundColor = backgroundColor,
                fractalXCenter = fractalXCenter,
                fractalYCenter = fractalYCenter,
                fractalZoom = fractalZoom,
                juliaCX = juliaCX,
                juliaCY = juliaCY,
                colormap = colormap,
                fractalIterations = fractalIterations
            )
        }
    }
}
