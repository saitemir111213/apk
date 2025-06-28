package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DrawingType {
    POLYGON, POLYLINE, POINT
}

@Entity(tableName = "drawings")
data class Drawing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reportId: Int, // Foreign key to link to a Report
    val type: DrawingType,
    val points: String, // Storing points as a serialized string (e.g., JSON)
    val taskType: String,
    val description: String,
    val address: String?, // For reverse-geocoded address
    val timestamp: Long,
    val hours: Int = 0
) 