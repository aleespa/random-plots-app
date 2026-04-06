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
    val param1: Double? = null,
    val param2: Double? = null,
    val param3: Double? = null,
    val param4: Double? = null,
    val param5: Double? = null,
    val param6: Double? = null,
    val param7: Double? = null,
    val param8: Double? = null,
    val param9: Double? = null,
    val param10: Double? = null,
    val colormap: String? = null,
    val iterations: Int? = null
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
        private var param1: Double? = null
        private var param2: Double? = null
        private var param3: Double? = null
        private var param4: Double? = null
        private var param5: Double? = null
        private var param6: Double? = null
        private var param7: Double? = null
        private var param8: Double? = null
        private var param9: Double? = null
        private var param10: Double? = null
        private var colormap: String? = null
        private var iterations: Int? = null

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

        fun setParam1(v: Double?) = apply { this.param1 = v }
        fun setParam2(v: Double?) = apply { this.param2 = v }
        fun setParam3(v: Double?) = apply { this.param3 = v }
        fun setParam4(v: Double?) = apply { this.param4 = v }
        fun setParam5(v: Double?) = apply { this.param5 = v }
        fun setParam6(v: Double?) = apply { this.param6 = v }
        fun setParam7(v: Double?) = apply { this.param7 = v }
        fun setParam8(v: Double?) = apply { this.param8 = v }
        fun setParam9(v: Double?) = apply { this.param9 = v }
        fun setParam10(v: Double?) = apply { this.param10 = v }

        fun setColormap(colormap: String?) = apply { this.colormap = colormap }
        fun setIterations(iterations: Int?) = apply { this.iterations = iterations }

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
                param1 = param1,
                param2 = param2,
                param3 = param3,
                param4 = param4,
                param5 = param5,
                param6 = param6,
                param7 = param7,
                param8 = param8,
                param9 = param9,
                param10 = param10,
                colormap = colormap,
                iterations = iterations
            )
        }
    }
}
