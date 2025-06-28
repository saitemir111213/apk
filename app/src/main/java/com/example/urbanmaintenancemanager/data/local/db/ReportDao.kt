package com.example.urbanmaintenancemanager.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urbanmaintenancemanager.data.local.model.Report
import com.example.urbanmaintenancemanager.data.local.model.ReportCount
import com.example.urbanmaintenancemanager.data.local.model.WorkHours
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report): Long

    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE date BETWEEN :startTime AND :endTime ORDER BY date DESC")
    suspend fun getReportsInDateRange(startTime: Long, endTime: Long): List<Report>

    @Query("SELECT STRFTIME('%Y-%m-%d', date / 1000, 'unixepoch') as date, COUNT(id) as count FROM reports GROUP BY STRFTIME('%Y-%m-%d', date / 1000, 'unixepoch') ORDER BY date DESC")
    fun getReportCounts(): Flow<List<ReportCount>>

    @Query("""
        SELECT STRFTIME('%Y-%m-%d', r.date / 1000, 'unixepoch') as date, SUM(d.hours) as totalHours
        FROM reports r
        JOIN drawings d ON r.id = d.reportId
        GROUP BY STRFTIME('%Y-%m-%d', r.date / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    fun getWorkHoursPerDay(): Flow<List<WorkHours>>
}
