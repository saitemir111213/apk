package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Entity

@Entity(primaryKeys = ["drawingId", "workerId"])
data class DrawingWorkerCrossRef(
    val drawingId: Int,
    val workerId: Int
) 