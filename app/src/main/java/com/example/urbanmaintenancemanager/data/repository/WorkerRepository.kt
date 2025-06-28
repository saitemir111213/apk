package com.example.urbanmaintenancemanager.data.repository

import com.example.urbanmaintenancemanager.data.local.db.WorkerDao
import com.example.urbanmaintenancemanager.data.local.model.Worker
import kotlinx.coroutines.flow.Flow

class WorkerRepository(private val workerDao: WorkerDao) {

    fun getAllWorkers(): Flow<List<Worker>> {
        return workerDao.getAllWorkers()
    }

    suspend fun insertWorker(worker: Worker) {
        workerDao.insertWorker(worker)
    }

    suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker)
    }

    suspend fun deleteWorker(worker: Worker) {
        workerDao.deleteWorker(worker)
    }
} 