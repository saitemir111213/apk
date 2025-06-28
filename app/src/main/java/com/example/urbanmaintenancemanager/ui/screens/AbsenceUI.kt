package com.example.urbanmaintenancemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.babakcode.materialdatepicker.DateType
import com.babakcode.materialdatepicker.PersianDatePickerDialog
import com.babakcode.materialdatepicker.utils.PersianCalendar
import com.example.urbanmaintenancemanager.R
import com.example.urbanmaintenancemanager.data.local.model.Absence
import com.example.urbanmaintenancemanager.data.local.model.Worker
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
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