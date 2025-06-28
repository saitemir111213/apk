package com.example.urbanmaintenancemanager.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanmaintenancemanager.data.local.model.Drawing
import com.example.urbanmaintenancemanager.data.local.model.DrawingType
import com.example.urbanmaintenancemanager.data.local.model.Report
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import com.example.urbanmaintenancemanager.data.network.GeocodingService
import com.example.urbanmaintenancemanager.data.repository.GroupRepository
import com.example.urbanmaintenancemanager.data.repository.ReportRepository
import com.example.urbanmaintenancemanager.data.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val workerRepository: WorkerRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _drawingMode = MutableStateFlow<DrawingType?>(null)
    val drawingMode: StateFlow<DrawingType?> = _drawingMode

    private val _currentPoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val currentPoints: StateFlow<List<GeoPoint>> = _currentPoints

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address

    val allWorkers: StateFlow<List<Worker>> = workerRepository.getAllWorkers()
        .stateIn(
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

    fun setDrawingMode(mode: DrawingType?) {
        _drawingMode.value = mode
        if (mode == null) {
            clearCurrentPoints()
            _address.value = null
        }
    }

    fun addPoint(point: GeoPoint) {
        _currentPoints.value = _currentPoints.value + point
    }

    private fun clearCurrentPoints() {
        _currentPoints.value = emptyList()
    }

    fun fetchAddressForCurrentDrawing() {
        if (_currentPoints.value.isNotEmpty()) {
            viewModelScope.launch {
                _address.value = GeocodingService.getAddressFromCoordinates(_currentPoints.value.first())
            }
        }
    }

    fun saveReportAndDrawings(
        taskType: String,
        description: String,
        workers: List<Worker>,
        timestamp: Long,
        hours: Int
    ) {
        viewModelScope.launch {
            val drawingMode = _drawingMode.value ?: return@launch
            val points = _currentPoints.value
            if (points.isEmpty()) return@launch

            val report = Report(
                date = timestamp,
                description = "Report for task: $taskType"
            )

            val drawing = Drawing(
                reportId = 0, // Will be replaced by the actual reportId
                type = drawingMode,
                points = points.joinToString(";") { "${it.latitude},${it.longitude}" },
                taskType = taskType,
                description = description,
                address = _address.value,
                timestamp = timestamp,
                hours = hours
            )

            // Here we assume one drawing per report for simplicity.
            // This can be extended to support multiple drawings.
            reportRepository.insertReportWithDrawings(report, drawing, workers)

            // Reset state
            setDrawingMode(null)
        }
    }
}

class ReportViewModelFactory(
    private val reportRepository: ReportRepository,
    private val workerRepository: WorkerRepository,
    private val groupRepository: GroupRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(reportRepository, workerRepository, groupRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 