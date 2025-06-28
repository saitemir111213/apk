package com.example.urbanmaintenancemanager.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urbanmaintenancemanager.data.local.model.Drawing
import com.example.urbanmaintenancemanager.data.local.model.DrawingWorkerCrossRef
import com.example.urbanmaintenancemanager.data.local.model.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: Drawing): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDrawingWorkerCrossRef(crossRef: DrawingWorkerCrossRef)

    @Query("SELECT * FROM drawings WHERE reportId = :reportId")
    suspend fun getDrawingsForReport(reportId: Int): List<Drawing>

    @Query("""
        SELECT w.* FROM workers w
        INNER JOIN DrawingWorkerCrossRef dwcr ON w.id = dwcr.workerId
        WHERE dwcr.drawingId = :drawingId
    """)
    suspend fun getWorkersForDrawing(drawingId: Int): List<Worker>
} 