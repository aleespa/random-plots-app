package com.aleespa.randomsquare.data

import android.content.Context
import android.util.Log
import androidx.room.Room

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: try {
                buildDatabase(context).also {
                    // Try to open the database to catch corruption early
                    it.openHelper.writableDatabase
                    instance = it
                }
            } catch (e: Exception) {
                Log.e("DatabaseProvider", "Database error detected, recreating database", e)
                context.deleteDatabase("images_database")
                buildDatabase(context).also { instance = it }
            }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "images_database"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration(true)
            .build()
    }
}