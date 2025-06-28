package com.example.urbanmaintenancemanager.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babakcode.materialdatepicker.DateType
import com.babakcode.materialdatepicker.PersianDatePickerDialog
import com.babakcode.materialdatepicker.utils.PersianCalendar
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.UrbanMaintenanceManagerApplication
import com.example.urbanmaintenancemanager.data.local.model.Absence
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfilesScreen(
    modifier: Modifier = Modifier,
    viewModel: WorkerViewModel = viewModel(
        factory = WorkerViewModelFactory(
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).workerRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).groupRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).absenceRepository
        )
    )
) {
    val workers by viewModel.filteredWorkers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val groups by viewModel.allGroups.collectAsState()
    var openAddWorkerDialog by remember { mutableStateOf(false) }
    var openAddGroupDialog by remember { mutableStateOf(false) }
    var workerForAbsence by remember { mutableStateOf<Worker?>(null) }
    var workerForOvertime by remember { mutableStateOf<Worker?>(null) }

    val groupedWorkers = workers.groupBy { worker ->
        groups.find { it.id == worker.groupId }?.name ?: stringResource(R.string.unassigned)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_worker_profiles)) },
                actions = {
                    IconButton(onClick = { openAddGroupDialog = true }) {
                        Icon(painter = painterResource(id = R.drawable.ic_group_add), contentDescription = stringResource(R.string.manage_groups))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openAddWorkerDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_worker))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(stringResource(R.string.search_worker_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            if (workers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = R.string.no_workers_found), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    groupedWorkers.forEach { (groupName, workersInGroup) ->
                        item {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(workersInGroup, key = { it.id }) { worker ->
                            WorkerCard(
                                worker = worker,
                                onAddAbsenceClick = { workerForAbsence = it },
                                onAddOvertimeClick = { workerForOvertime = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (openAddWorkerDialog) {
        AddWorkerDialog(
            groups = groups,
            onDismiss = { openAddWorkerDialog = false },
            onConfirm = { name, phone, groupId ->
                viewModel.addWorker(Worker(name = name, phoneNumber = phone, groupId = groupId))
                openAddWorkerDialog = false
            }
        )
    }

    if (openAddGroupDialog) {
        AddGroupDialog(
            onDismiss = { openAddGroupDialog = false },
            onConfirm = { groupName ->
                viewModel.addGroup(WorkerGroup(name = groupName))
                openAddGroupDialog = false
            }
        )
    }

    workerForAbsence?.let { worker ->
        AddAbsenceDialog(
            worker = worker,
            onDismiss = { workerForAbsence = null },
            onSave = { absence ->
                viewModel.addAbsence(absence)
                workerForAbsence = null
            }
        )
    }

    workerForOvertime?.let { worker ->
        AddOvertimeDialog(
            worker = worker,
            onDismiss = { workerForOvertime = null },
            onConfirm = { hours ->
                viewModel.addOvertime(worker, hours)
                workerForOvertime = null
            }
        )
    }
}

@Composable
fun AddOvertimeDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_overtime_for, worker.name)) },
        text = {
            Column {
                Text(stringResource(R.string.current_overtime_hours, worker.overtimeHours))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = hours,
                    onValueChange = {
                        hours = it.filter { char -> char.isDigit() }
                        isError = it.toIntOrNull() == null && it.isNotEmpty()
                    },
                    label = { Text(stringResource(R.string.new_overtime_hours)) },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (isError) {
                    Text(
                        stringResource(R.string.enter_valid_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hoursInt = hours.toIntOrNull()
                    if (hoursInt != null) {
                        onConfirm(hoursInt)
                    } else {
                        isError = true
                    }
                },
                enabled = hours.isNotBlank() && !isError
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun WorkerCard(
    worker: Worker,
    onAddAbsenceClick: (Worker) -> Unit,
    onAddOvertimeClick: (Worker) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = worker.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "اضافه‌کاری: ${worker.overtimeHours} ساعت",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.expand),
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    worker.phoneNumber?.let { phone ->
                        InfoRow(icon = Icons.Default.Call, text = phone) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    }
                    worker.email?.let { email ->
                        InfoRow(icon = Icons.Default.Email, text = email) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                            }
                            context.startActivity(Intent.createChooser(intent, "ارسال ایمیل..."))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onAddAbsenceClick(worker) }) {
                            Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.add_absence), tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { onAddOvertimeClick(worker) }) {
                            Icon(Icons.Default.MoreTime, contentDescription = stringResource(R.string.add_overtime), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AddAbsenceDialog(worker: Worker, onDismiss: () -> Unit, onSave: (Absence) -> Unit) {
    var reason by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(PersianDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val pfd = PersianDateFormat("Y/m/d")

    if (showDatePicker) {
        val persianCalendar = PersianCalendar()
        persianCalendar.timeInMillis = selectedDate.time
        PersianDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            persianCalendar = persianCalendar,
            onDateChanged = { year, month, day ->
                val newDate = PersianDate()
                newDate.shYear = year
                newDate.shMonth = month
                newDate.shDay = day
                selectedDate = newDate
            },
            dateType = DateType.Jalali
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_absence) + " for " + worker.name) },
        text = {
            Column {
                OutlinedTextField(
                    value = pfd.format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.date)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.absence_reason)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val absence = Absence(
                        workerId = worker.id,
                        date = selectedDate.time,
                        reason = reason
                    )
                    onSave(absence)
                },
                enabled = reason.isNotBlank()
            ) {
                Text(stringResource(R.string.save_absence))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun startPhoneCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    context.startActivity(intent)
}

private fun sendSms(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
    context.startActivity(intent)
}

@Composable
fun AddWorkerDialog(
    groups: List<WorkerGroup>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<WorkerGroup?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_worker)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.worker_name)) }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_number)) }
                )
                Box {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: stringResource(R.string.select_group),
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Toggle dropdown")
                            }
                        },
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroup = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, phone, selectedGroup?.id ?: 0)
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_worker)) },
        text = { Text(stringResource(R.string.delete_worker_confirmation)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AddGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var groupName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_group)) },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text(stringResource(R.string.group_name)) }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(groupName) },
                enabled = groupName.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 