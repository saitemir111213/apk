package com.example.urbanmaintenancemanager.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.urbanmaintenancemanager.data.local.model.ReportWithDetails
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import com.example.urbanmaintenancemanager.data.repository.GroupRepository
import com.example.urbanmaintenancemanager.data.repository.ReportRepository
import com.example.urbanmaintenancemanager.util.HtmlGenerator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ExportUiState(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val selectedGroupIds: Set<Int> = emptySet(),
    val allGroups: List<WorkerGroup> = emptyList(),
    val reportData: List<ReportWithDetails>? = null,
    val isGenerating: Boolean = false
)

class ExportViewModel(
    private val application: Application,
    private val groupRepository: GroupRepository,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExportEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(allGroups = groups) }
            }
        }
    }

    fun setStartDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
        }
        _uiState.update { it.copy(startDate = calendar.timeInMillis) }
    }

    fun setEndDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
        }
        _uiState.update { it.copy(endDate = calendar.timeInMillis) }
    }

    fun toggleGroupSelection(groupId: Int) {
        val currentSelection = _uiState.value.selectedGroupIds
        val newSelection = if (currentSelection.contains(groupId)) {
            currentSelection - groupId
        } else {
            currentSelection + groupId
        }
        _uiState.update { it.copy(selectedGroupIds = newSelection) }
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, reportData = null) }
            val startTime = _uiState.value.startDate
            val endTime = _uiState.value.endDate
            if (startTime == null || endTime == null) {
                _uiState.update { it.copy(isGenerating = false) }
                return@launch
            }

            val reports = reportRepository.getReportsWithDetails(
                startTime = startTime,
                endTime = endTime,
                groupIds = _uiState.value.selectedGroupIds.toList()
            )
            _uiState.update { it.copy(isGenerating = false, reportData = reports) }

            if (reports.isNotEmpty()) {
                val reportFileUri = createReportZip(reports)
                _events.send(ExportEvent.ReportZipReady(reportFileUri))
            } else {
                // Handle case with no reports found
            }
        }
    }

    private suspend fun createReportZip(reports: List<ReportWithDetails>): Uri {
        val htmlGenerator = HtmlGenerator(application)
        val pfd = saman.zamani.persiandate.PersianDateFormat("Y-m-d")
        val dateRange = "${pfd.format(PersianDate(_uiState.value.startDate!!))} تا ${pfd.format(PersianDate(_uiState.value.endDate!!))}"
        val htmlContent = htmlGenerator.generate(reports, dateRange)

        val reportsDir = File(application.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        // Save HTML
        val htmlFile = File(reportsDir, "report.html")
        htmlFile.writeText(htmlContent)

        // Copy CSS
        val cssFile = File(reportsDir, "style.css")
        application.assets.open("style.css").use { input ->
            FileOutputStream(cssFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Create Zip
        val zipFile = File(application.cacheDir, "report.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            // Add HTML
            zipOut.putNextEntry(ZipEntry(htmlFile.name))
            htmlFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()

            // Add CSS
            zipOut.putNextEntry(ZipEntry(cssFile.name))
            cssFile.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }
        
        return androidx.core.content.FileProvider.getUriForFile(application, "${application.packageName}.provider", zipFile)
    }
}

class ExportViewModelFactory(
    private val application: Application,
    private val groupRepository: GroupRepository,
    private val reportRepository: ReportRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportViewModel::class.java)) {
            return ExportViewModel(application, groupRepository, reportRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class ExportEvent {
    data class ReportZipReady(val zipUri: Uri) : ExportEvent()
} 