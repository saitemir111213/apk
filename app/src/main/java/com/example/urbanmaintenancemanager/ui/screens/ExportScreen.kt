package com.example.urbanmaintenancemanager.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babakcode.persianmaterialdatepicker.PersianDatePickerDialog
import com.babakcode.persianmaterialdatepicker.utils.PersianCalendar
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.UrbanMaintenanceManagerApplication
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    exportViewModel: ExportViewModel = viewModel(
        factory = ExportViewModelFactory(
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication),
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).groupRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).reportRepository
        )
    )
) {
    val uiState by exportViewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf<Boolean?>(null) } // null: none, true: start, false: end
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        exportViewModel.events.collect { event ->
            when (event) {
                is ExportEvent.ReportZipReady -> {
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, event.zipUri)
                        type = "application/zip"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_report)))
                }
            }
        }
    }

    if (showDatePicker != null) {
        val persianCalendar = PersianCalendar()
        val initialDate = if (showDatePicker == true) uiState.startDate else uiState.endDate
        initialDate?.let { persianCalendar.timeInMillis = it }

        PersianDatePickerDialog(
            initialDate = persianCalendar,
            onDismissRequest = { showDatePicker = null },
            onDateSelected = { year, month, day ->
                if (showDatePicker == true) {
                    exportViewModel.setStartDate(year, month, day)
                } else {
                    exportViewModel.setEndDate(year, month, day)
                }
                showDatePicker = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_export)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { exportViewModel.generateReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = uiState.startDate != null && uiState.endDate != null && !uiState.isGenerating
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(id = R.string.generate_report_and_share))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.report_filters_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.date_range),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DateSelector(
                                label = stringResource(id = R.string.start_date),
                                dateText = uiState.startDate?.toPersianDateString() ?: "",
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            DateSelector(
                                label = stringResource(id = R.string.end_date),
                                dateText = uiState.endDate?.toPersianDateString() ?: "",
                                onClick = { showDatePicker = false },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = stringResource(id = R.string.select_groups),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        uiState.allGroups.forEach { group ->
                            GroupCheckboxItem(
                                group = group,
                                isSelected = uiState.selectedGroupIds.contains(group.id),
                                onToggle = { exportViewModel.toggleGroupSelection(group.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCheckboxItem(group: WorkerGroup, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = group.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun DateSelector(
    label: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = dateText,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier.clickable(onClick = onClick),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = null)
        },
        shape = RoundedCornerShape(8.dp)
    )
}

private fun Long.toPersianDateString(): String {
    val persianCalendar = PersianCalendar()
    persianCalendar.timeInMillis = this
    return persianCalendar.persianLongDate
} 