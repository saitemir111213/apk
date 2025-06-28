package com.example.urbanmaintenancemanager

import android.app.Application
import com.example.urbanmaintenancemanager.data.local.db.AppDatabase
import com.example.urbanmaintenancemanager.data.repository.WorkerRepository
import com.example.urbanmaintenancemanager.data.repository.ReportRepository
import com.example.urbanmaintenancemanager.data.repository.GroupRepository
import com.example.urbanmaintenancemanager.data.repository.AbsenceRepository

class UrbanMaintenanceManagerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val workerRepository by lazy { WorkerRepository(database.workerDao()) }
    val reportRepository by lazy { ReportRepository(database.reportDao(), database.drawingDao()) }
    val groupRepository by lazy { GroupRepository(database.workerGroupDao()) }
    val absenceRepository by lazy { AbsenceRepository(database.absenceDao()) }
} 