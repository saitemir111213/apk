package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workers",
    foreignKeys = [ForeignKey(
        entity = WorkerGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val dailyLeave: Int = 0,
    val hourlyLeave: Int = 0,
    val overtimeHours: Int = 0,
    val workHours: Int = 0,
    val groupId: Int? = null
) 