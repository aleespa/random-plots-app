package com.alejandro.randomplots.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images ORDER BY timestamp DESC")
    suspend fun getAllImages(): List<ImageEntity>

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImageById(id: Int)
}