package com.example.urbanmaintenancemanager.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babakcode.materialdatepicker.PersianDatePickerDialog
import com.babakcode.materialdatepicker.utils.PersianCalendar
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.UrbanMaintenanceManagerApplication
import com.example.urbanmaintenancemanager.data.local.model.DrawingType
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import com.example.urbanmaintenancemanager.ui.screens.report.ReportViewModel
import com.example.urbanmaintenancemanager.ui.screens.report.ReportViewModelFactory
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
    viewModel: ReportViewModel = viewModel(
        factory = ReportViewModelFactory(
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).reportRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).workerRepository,
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).groupRepository
        )
    )
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDrawingTools by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val drawingMode by viewModel.drawingMode.collectAsState()
    val workers by viewModel.allWorkers.collectAsState()
    val groups by viewModel.allGroups.collectAsState()
    val address by viewModel.address.collectAsState()

    if (showSaveDialog) {
        SaveReportDialog(
            workers = workers,
            groups = groups,
            address = address,
            onDismiss = { showSaveDialog = false },
            onSave = { taskType, description, selectedWorkers, timestamp, hours ->
                viewModel.saveReportAndDrawings(taskType, description, selectedWorkers, timestamp, hours)
                showSaveDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_daily_report)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (drawingMode == null) {
                FloatingActionButton(onClick = { showDrawingTools = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_report)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val mapView = rememberMapViewWithLifecycle(context)
            MapViewContainer(mapView = mapView, viewModel = viewModel)

            if (showDrawingTools) {
                ModalBottomSheet(
                    onDismissRequest = { showDrawingTools = false },
                    sheetState = sheetState
                ) {
                    DrawingToolsPanel(onToolSelected = {
                        viewModel.setDrawingMode(it)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showDrawingTools = false
                            }
                        }
                    })
                }
            }

            if (drawingMode != null) {
                DrawingActions(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onConfirm = {
                        viewModel.fetchAddressForCurrentDrawing()
                        showSaveDialog = true
                    },
                    onCancel = { viewModel.setDrawingMode(null) }
                )
            }
        }
    }
}

@Composable
fun DrawingToolsPanel(onToolSelected: (DrawingType) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.select_drawing_tool),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrawingTool(iconRes = R.drawable.ic_polygon, label = stringResource(R.string.polygon), onClick = { onToolSelected(DrawingType.POLYGON) })
            DrawingTool(iconRes = R.drawable.ic_polyline, label = stringResource(R.string.polyline), onClick = { onToolSelected(DrawingType.POLYLINE) })
            DrawingTool(iconRes = R.drawable.ic_marker, label = stringResource(R.string.marker), onClick = { onToolSelected(DrawingType.POINT) })
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MapViewContainer(mapView: MapView, viewModel: ReportViewModel) {
    val drawingMode by viewModel.drawingMode.collectAsState()
    val currentPoints by viewModel.currentPoints.collectAsState()

    AndroidView({ mapView }) {
        // Update logic
        it.overlays.removeAll { it is Polygon || it is Polyline }

        if (currentPoints.isNotEmpty()) {
            when (drawingMode) {
                DrawingType.POLYGON -> {
                    val polygon = Polygon().apply {
                        points = currentPoints
                        fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f).toArgb()
                        strokeColor = MaterialTheme.colorScheme.primary.toArgb()
                        strokeWidth = 5f
                    }
                    it.overlays.add(polygon)
                }
                DrawingType.POLYLINE -> {
                    val polyline = Polyline().apply {
                        points = currentPoints
                        color = MaterialTheme.colorScheme.primary.toArgb()
                        width = 5f
                    }
                    it.overlays.add(polyline)
                }
                DrawingType.POINT -> {
                    // Points are usually handled by markers, which should be added separately
                }
                null -> {}
            }
        }
        it.invalidate() // Redraw the map
    }
}

@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map // Ensure you have an ID for the map in res/values/ids.xml or similar
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(35.6892, 51.3890)) // Tehran
        }
    }
    // Add lifecycle observer if needed, for now we just remember it.
    return mapView
}

@Composable
fun DrawingActions(modifier: Modifier = Modifier, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text(text = stringResource(R.string.cancel_drawing))
        }
        Button(onClick = onConfirm) {
            Text(text = stringResource(R.string.finish_drawing))
        }
    }
}

@Composable
fun DrawingTool(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveReportDialog(
    workers: List<Worker>,
    groups: List<WorkerGroup>,
    address: String,
    onDismiss: () -> Unit,
    onSave: (String, String, List<Worker>, Long, Int) -> Unit
) {
    var taskType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedWorkers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedDate by remember { mutableStateOf(PersianDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("8") }

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
        )
    }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.save_report),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        OutlinedTextField(
                            value = taskType,
                            onValueChange = { taskType = it },
                            label = { Text(stringResource(R.string.task_type)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.description)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.address)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
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
                    }
                    item {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it.filter { char -> char.isDigit() } },
                            label = { Text(stringResource(R.string.hours_worked)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.select_workers),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    val groupedWorkers = workers.groupBy { worker ->
                        groups.find { it.id == worker.groupId }?.name ?: stringResource(R.string.unassigned)
                    }

                    groupedWorkers.forEach { (groupName, workersInGroup) ->
                        item {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(workersInGroup) { worker ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = selectedWorkers.contains(worker.id),
                                        onValueChange = {
                                            selectedWorkers = if (it) {
                                                selectedWorkers + worker.id
                                            } else {
                                                selectedWorkers - worker.id
                                            }
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedWorkers.contains(worker.id),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = worker.name)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                taskType,
                                description,
                                workers.filter { selectedWorkers.contains(it.id) },
                                selectedDate.time,
                                hours.toIntOrNull() ?: 0
                            )
                        },
                        enabled = taskType.isNotBlank() && description.isNotBlank() && selectedWorkers.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}