package com.example.urbanmaintenancemanager.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urbanmaintenancemanager.data.local.model.Absence
import kotlinx.coroutines.flow.Flow

@Dao
interface AbsenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsence(absence: Absence)

    @Query("SELECT * FROM absences WHERE workerId = :workerId ORDER BY date DESC")
    fun getAbsencesForWorker(workerId: Int): Flow<List<Absence>>
} 