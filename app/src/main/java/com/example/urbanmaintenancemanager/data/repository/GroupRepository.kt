package com.example.urbanmaintenancemanager.data.repository

import com.example.urbanmaintenancemanager.data.local.db.WorkerGroupDao
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val workerGroupDao: WorkerGroupDao) {
    fun getAllGroups(): Flow<List<WorkerGroup>> = workerGroupDao.getAllGroups()

    suspend fun insertGroup(group: WorkerGroup) {
        workerGroupDao.insertGroup(group)
    }
} 