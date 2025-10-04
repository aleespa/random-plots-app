package com.aleespa.randomsquare.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ImageEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}
