package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.ScraperProject

@Database(entities = [ScraperProject::class], version = 1, exportSchema = false)
abstract class ScraperDatabase : RoomDatabase() {
    abstract fun scraperDao(): ScraperDao

    companion object {
        @Volatile
        private var INSTANCE: ScraperDatabase? = null

        fun getDatabase(context: Context): ScraperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScraperDatabase::class.java,
                    "student_data_scraper_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
