package com.aleespa.randomsquare.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long // Returns the ID of the inserted image

    @Query("SELECT * FROM images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<ImageEntity>> // Automatically emits updates

    @Query("SELECT * FROM images WHERE isDarkMode = :isDarkMode ORDER BY timestamp DESC")
    fun getImageByIsDarkMode(isDarkMode: Boolean): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImageById(id: Int)

    @Query("""
        SELECT * FROM images 
        WHERE 
        (:isDarkMode IS NULL OR isDarkMode = :isDarkMode) 
        AND 
        (:imageType IS NULL OR imageType = :imageType)
        ORDER BY timestamp DESC
    """)
    suspend fun getFilteredImages(isDarkMode: Boolean?, imageType: String?): List<ImageEntity>

}