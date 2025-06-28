package com.example.urbanmaintenancemanager.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "absences",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Absence(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workerId: Int,
    val date: Long,
    val reason: String
) 