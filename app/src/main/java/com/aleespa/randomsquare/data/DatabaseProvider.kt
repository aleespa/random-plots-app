package com.aleespa.randomsquare.data
import MIGRATION_1_2
import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "images_database")
                .addMigrations(MIGRATION_1_2)
                .build()
        }
        return instance!!
    }
}