package com.alejandro.randomplots.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images",)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val imageType: String,
    val timestamp: Long,
    val isDarkMode: Boolean
)
