package com.example.urbanmaintenancemanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanmaintenancemanager.data.local.model.Absence
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import com.example.urbanmaintenancemanager.data.repository.AbsenceRepository
import com.example.urbanmaintenancemanager.data.repository.GroupRepository
import com.example.urbanmaintenancemanager.data.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val workerRepository: WorkerRepository,
    private val groupRepository: GroupRepository,
    private val absenceRepository: AbsenceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _allWorkers = workerRepository.getAllWorkers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredWorkers: StateFlow<List<Worker>> =
        searchQuery.combine(_allWorkers) { query, workers ->
            if (query.isBlank()) {
                workers
            } else {
                workers.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGroups: StateFlow<List<WorkerGroup>> = groupRepository.getAllGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.insertWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            workerRepository.deleteWorker(worker)
        }
    }

    fun addGroup(group: WorkerGroup) {
        viewModelScope.launch {
            groupRepository.insertGroup(group)
        }
    }

    fun deleteGroup(group: WorkerGroup) {
        viewModelScope.launch {
            groupRepository.deleteGroup(group)
        }
    }

    fun getAbsencesForWorker(workerId: Int): StateFlow<List<Absence>> {
        return absenceRepository.getAbsencesForWorker(workerId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addAbsence(absence: Absence) {
        viewModelScope.launch {
            absenceRepository.insertAbsence(absence)
        }
    }

    fun addOvertime(worker: Worker, hours: Int) {
        viewModelScope.launch {
            val updatedWorker = worker.copy(overtimeHours = worker.overtimeHours + hours)
            workerRepository.updateWorker(updatedWorker)
        }
    }
}

class WorkerViewModelFactory(
    private val workerRepository: WorkerRepository,
    private val groupRepository: GroupRepository,
    private val absenceRepository: AbsenceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkerViewModel(workerRepository, groupRepository, absenceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 