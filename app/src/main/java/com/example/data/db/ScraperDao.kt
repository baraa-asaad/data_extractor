package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.ScraperProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ScraperDao {
    @Query("SELECT * FROM scraper_projects ORDER BY createdDate DESC")
    fun getAllProjects(): Flow<List<ScraperProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ScraperProject): Long

    @Query("DELETE FROM scraper_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
