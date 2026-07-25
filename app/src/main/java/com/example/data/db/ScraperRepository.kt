package com.example.data.db

import com.example.data.ScraperProject
import kotlinx.coroutines.flow.Flow

class ScraperRepository(private val dao: ScraperDao) {
    val allProjects: Flow<List<ScraperProject>> = dao.getAllProjects()

    suspend fun saveProject(project: ScraperProject): Long {
        return dao.insertProject(project)
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }
}
