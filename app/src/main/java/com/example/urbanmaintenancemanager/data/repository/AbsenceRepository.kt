package com.example.urbanmaintenancemanager.data.repository

import com.example.urbanmaintenancemanager.data.local.db.AbsenceDao
import com.example.urbanmaintenancemanager.data.local.model.Absence

class AbsenceRepository(private val absenceDao: AbsenceDao) {

    fun getAbsencesForWorker(workerId: Int) = absenceDao.getAbsencesForWorker(workerId)

    suspend fun insertAbsence(absence: Absence) {
        absenceDao.insertAbsence(absence)
    }
} 