package com.aleespa.randomplots.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images",)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String = "",
    val imageType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDarkMode: Boolean = false,
    val randomSeed: Long = 0,
    val backgroundColor: Int = 0
) {
    // Builder class
    class Builder {
        private var uri: String = ""
        private var imageType: String = ""
        private var timestamp: Long = System.currentTimeMillis()
        private var isDarkMode: Boolean = false
        private var randomSeed: Long = 0
        private var backgroundColor: Int = 0

        fun setUri(uri: String) = apply { this.uri = uri }
        fun setImageType(imageType: String) = apply { this.imageType = imageType }
        fun setTimestamp(timestamp: Long) = apply { this.timestamp = timestamp }
        fun setIsDarkMode(isDarkMode: Boolean) = apply { this.isDarkMode = isDarkMode }
        fun setRandomSeed(randomSeed: Long) = apply { this.randomSeed = randomSeed }
        fun setBackgroundColor(backgroundColor: Int) = apply { this.backgroundColor = backgroundColor }

        fun build(): ImageEntity {
            return ImageEntity(
                uri = uri,
                imageType = imageType,
                timestamp = timestamp,
                isDarkMode = isDarkMode,
                randomSeed = randomSeed,
                backgroundColor = backgroundColor
            )
        }
    }
}
