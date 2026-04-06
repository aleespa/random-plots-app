package com.aleespa.randomsquare.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ImageEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with the exact schema Room expects for v3
                db.execSQL("""
                    CREATE TABLE images_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        uri TEXT NOT NULL,
                        imageType TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isDarkMode INTEGER NOT NULL,
                        randomSeed INTEGER,
                        backgroundColor INTEGER NOT NULL,
                        fractalXCenter REAL,
                        fractalYCenter REAL,
                        fractalZoom REAL,
                        param1 REAL,
                        param2 REAL,
                        param3 REAL,
                        param4 REAL,
                        param5 REAL,
                        param6 REAL,
                        param7 REAL,
                        param8 REAL,
                        param9 REAL,
                        param10 REAL,
                        colormap TEXT,
                        iterations INTEGER
                    )
                """.trimIndent())

                // 2. Copy data from the v2 table
                // v2 columns: id, uri, imageType, timestamp, isDarkMode, randomSeed, backgroundColor
                // Plus, map the old juliaCX/juliaCY to param1/param2 if they existed (though they are new in v3)
                db.execSQL("""
                    INSERT INTO images_new (id, uri, imageType, timestamp, isDarkMode, randomSeed, backgroundColor)
                    SELECT id, uri, imageType, timestamp, isDarkMode, randomSeed, backgroundColor 
                    FROM images
                """.trimIndent())

                // 3. Remove the old table
                db.execSQL("DROP TABLE images")

                // 4. Rename the new table to 'images'
                db.execSQL("ALTER TABLE images_new RENAME TO images")
            }
        }
    }
}
