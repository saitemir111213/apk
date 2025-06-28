package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Embedded

data class ReportWithDetails(
    @Embedded
    val report: Report,
    val drawings: List<DrawingWithWorkers>
)

data class DrawingWithWorkers(
    @Embedded
    val drawing: Drawing,
    val workers: List<Worker>
) 