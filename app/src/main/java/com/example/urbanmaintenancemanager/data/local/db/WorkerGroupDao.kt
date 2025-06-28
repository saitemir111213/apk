package com.example.urbanmaintenancemanager.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: WorkerGroup)

    @Query("SELECT * FROM worker_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<WorkerGroup>>
} 