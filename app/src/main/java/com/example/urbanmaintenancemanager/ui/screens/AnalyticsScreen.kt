package com.example.urbanmaintenancemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.UrbanMaintenanceManagerApplication
import com.example.urbanmaintenancemanager.data.datastore.DarkThemeConfig
import com.example.urbanmaintenancemanager.data.local.model.ReportCount
import com.example.urbanmaintenancemanager.data.local.model.WorkHours
import com.example.urbanmaintenancemanager.ui.screens.analytics.AnalyticsViewModel
import com.example.urbanmaintenancemanager.ui.screens.analytics.AnalyticsViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun AnalyticsScreen(
    analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(
            (LocalContext.current.applicationContext as UrbanMaintenanceManagerApplication).reportRepository
        )
    )
) {
    val reportCounts by analyticsViewModel.reportCounts.collectAsState()
    val workHours by analyticsViewModel.workHours.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.title_analytics)) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (reportCounts.isEmpty() && workHours.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(R.string.no_data_available))
                    }
                }
            } else {
                if (reportCounts.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.daily_reports_count))
                            Box(modifier = Modifier.height(300.dp)) {
                                ReportsBarChart(reportCounts)
                            }
                        }
                    }
                }
                if (workHours.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.daily_work_hours))
                            Box(modifier = Modifier.height(300.dp)) {
                                WorkHoursBarChart(workHours)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsBarChart(reportCounts: List<ReportCount>) {
    val entries = reportCounts.mapIndexed { index, reportCount ->
        BarEntry(index.toFloat(), reportCount.count.toFloat())
    }
    val labels = reportCounts.map { it.date }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
            }
        },
        update = { chart ->
            val dataSet = BarDataSet(entries, "Reports").apply {
                color = Color.Blue.toArgb()
                valueTextColor = Color.Black.toArgb()
            }
            chart.data = BarData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun WorkHoursBarChart(workHours: List<WorkHours>) {
    val entries = workHours.mapIndexed { index, workHour ->
        BarEntry(index.toFloat(), workHour.totalHours.toFloat())
    }
    val labels = workHours.map { it.date }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
            }
        },
        update = { chart ->
            val dataSet = BarDataSet(entries, "Work Hours").apply {
                color = Color.Green.toArgb()
                valueTextColor = Color.Black.toArgb()
            }
            chart.data = BarData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
} 