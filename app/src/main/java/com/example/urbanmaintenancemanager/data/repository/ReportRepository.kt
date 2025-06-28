package com.example.urbanmaintenancemanager.data.repository

import androidx.room.Transaction
import com.example.urbanmaintenancemanager.data.local.db.DrawingDao
import com.example.urbanmaintenancemanager.data.local.db.ReportDao
import com.example.urbanmaintenancemanager.data.local.model.Drawing
import com.example.urbanmaintenancemanager.data.local.model.DrawingWorkerCrossRef
import com.example.urbanmaintenancemanager.data.local.model.Report
import com.example.urbanmaintenancemanager.data.local.model.ReportWithDetails
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.DrawingWithWorkers

class ReportRepository(
    private val reportDao: ReportDao,
    private val drawingDao: DrawingDao
) {
    fun getReportCounts() = reportDao.getReportCounts()

    fun getWorkHoursPerDay() = reportDao.getWorkHoursPerDay()

    @Transaction
    suspend fun getReportsWithDetails(
        startTime: Long,
        endTime: Long,
        groupIds: List<Int>
    ): List<ReportWithDetails> {
        val reports = reportDao.getReportsInDateRange(startTime, endTime)
        val reportsWithDetails = mutableListOf<ReportWithDetails>()

        for (report in reports) {
            val drawings = drawingDao.getDrawingsForReport(report.id)
            val drawingsWithWorkers = mutableListOf<DrawingWithWorkers>()
            var reportMatchesGroup = false

            for (drawing in drawings) {
                val workers = drawingDao.getWorkersForDrawing(drawing.id)
                if (groupIds.isEmpty() || workers.any { it.groupId in groupIds }) {
                    reportMatchesGroup = true
                    drawingsWithWorkers.add(DrawingWithWorkers(drawing, workers))
                }
            }
            if (reportMatchesGroup) {
                reportsWithDetails.add(ReportWithDetails(report, drawingsWithWorkers))
            }
        }
        return reportsWithDetails
    }

    @Transaction
    suspend fun insertReportWithDrawings(report: Report, drawing: Drawing, workers: List<Worker>) {
        val reportId = reportDao.insertReport(report)
        // Room returns the new rowId for an insert.
        val drawingId = drawingDao.insertDrawing(drawing.copy(reportId = reportId.toInt()))

        workers.forEach { worker ->
            drawingDao.insertDrawingWorkerCrossRef(
                DrawingWorkerCrossRef(drawingId = drawingId.toInt(), workerId = worker.id)
            )
        }
    }
} 