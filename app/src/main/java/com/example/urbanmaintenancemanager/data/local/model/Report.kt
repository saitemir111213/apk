package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long, // Storing date as timestamp for easy sorting and querying
    val description: String
) 