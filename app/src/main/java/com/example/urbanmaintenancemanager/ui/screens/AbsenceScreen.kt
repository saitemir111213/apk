package com.example.urbanmaintenancemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.UrbanMaintenanceManagerApplication
import com.example.urbanmaintenancemanager.data.local.model.Worker
import saman.zamani.persiandate.PersianDateFormat

@Composable
fun AbsenceScreen(
    viewModel: WorkerViewModel = viewModel(
        factory = WorkerViewModelFactory(
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).workerRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).groupRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).absenceRepository
        )
    )
) {
    val workers by viewModel.allWorkers.collectAsState()
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showAddAbsenceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.absences)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (selectedWorker == null) {
                // Worker selection list
                Text(text = stringResource(R.string.select_worker_to_manage_absences), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(workers) { worker ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedWorker = worker }
                        ) {
                            Text(text = worker.name, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            } else {
                // Absence management for the selected worker
                AbsenceManagementView(
                    worker = selectedWorker!!,
                    viewModel = viewModel,
                    onAddAbsenceClick = { showAddAbsenceDialog = true },
                    onBack = { selectedWorker = null }
                )
            }
        }
    }

    if (showAddAbsenceDialog && selectedWorker != null) {
        AddAbsenceDialog(
            worker = selectedWorker!!,
            onDismiss = { showAddAbsenceDialog = false },
            onSave = { absence ->
                viewModel.addAbsence(absence)
                showAddAbsenceDialog = false
            }
        )
    }
}

@Composable
fun AbsenceManagementView(
    worker: Worker,
    viewModel: WorkerViewModel,
    onAddAbsenceClick: () -> Unit,
    onBack: () -> Unit
) {
    val absences by viewModel.getAbsencesForWorker(worker.id).collectAsState()
    val pfd = PersianDateFormat("Y/m/d")

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = worker.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddAbsenceClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(id = R.string.add_absence))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.absences), style = MaterialTheme.typography.titleMedium)
        if (absences.isEmpty()) {
            Text(stringResource(id = R.string.no_absences_recorded))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(absences) { absence ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = pfd.format(absence.date))
                            Text(text = absence.reason)
                        }
                    }
                }
            }
        }
    }
} 