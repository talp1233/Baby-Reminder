package com.example.babyreminder

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val schedulePrefs = remember { context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE) }

    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val startTimeState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    val endTimeState = rememberTimePickerState(initialHour = 9, initialMinute = 0)

    val is24HourFormat = DateFormat.is24HourFormat(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_add_schedule_title), fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.settings_select_days), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Days in two rows
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    days.take(4).forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day, fontSize = 12.sp) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    days.drop(4).forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.settings_select_time_range), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showStartTimePicker = true }) {
                    Text(formatTime(startTimeState.hour, startTimeState.minute, is24HourFormat), fontSize = 16.sp)
                }
                Text("-")
                TextButton(onClick = { showEndTimePicker = true }) {
                     Text(formatTime(endTimeState.hour, endTimeState.minute, is24HourFormat), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (selectedDays.isNotEmpty()) {
                        val newRule = ScheduleRule(
                            id = UUID.randomUUID().toString(),
                            days = selectedDays,
                            startTime = formatTime(startTimeState.hour, startTimeState.minute, true),
                            endTime = formatTime(endTimeState.hour, endTimeState.minute, true)
                        )
                        val existingRules = schedulePrefs.getStringSet("rules", emptySet()) ?: emptySet()
                        val newRuleString = ruleToString(newRule)
                        schedulePrefs.edit().putStringSet("rules", existingRules + newRuleString).apply()
                        onNavigateBack()
                    }
                },
                enabled = selectedDays.isNotEmpty()
            ) {
                Text(stringResource(R.string.settings_save_schedule))
            }
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { showStartTimePicker = false },
            title = stringResource(R.string.settings_start_time)
        ) {
            TimePicker(state = startTimeState, colors = TimePickerDefaults.colors())
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { showEndTimePicker = false },
            title = stringResource(R.string.settings_end_time)
        ) {
            TimePicker(state = endTimeState, colors = TimePickerDefaults.colors())
        }
    }
}

@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, title: String, content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
    ) {
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.large
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(16.dp))
                content()
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text(stringResource(R.string.settings_ok)) }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val format = if (is24Hour) "HH:mm" else "h:mm a"
    return SimpleDateFormat(format, Locale.getDefault()).format(calendar.time)
}
