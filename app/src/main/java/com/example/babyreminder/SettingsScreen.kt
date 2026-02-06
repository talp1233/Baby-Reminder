package com.example.babyreminder

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to represent a single scheduling rule
data class ScheduleRule(val id: String, val days: Set<String>, val startTime: String, val endTime: String)

// Helper to convert rule to string for storage
fun ruleToString(rule: ScheduleRule): String {
    return "${rule.id}|${rule.days.joinToString(",")}|${rule.startTime}|${rule.endTime}"
}

// Helper to convert string back to rule
fun stringToRule(ruleString: String): ScheduleRule? {
    return try {
        val parts = ruleString.split('|')
        if (parts.size != 4) return null
        val days = if (parts[1].isBlank()) emptySet() else parts[1].split(',').toSet()
        ScheduleRule(id = parts[0], days = days, startTime = parts[2], endTime = parts[3])
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    deviceNames: Set<String>,
    onAddDevice: (String) -> Unit,
    onRemoveDevice: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAddSchedule: () -> Unit
) {
    val context = LocalContext.current
    val mainPrefs = remember { context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE) }
    val schedulePrefs = remember { context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE) }

    var enableSound by remember { mutableStateOf(mainPrefs.getBoolean("enable_sound", true)) }
    var defaultYes by remember { mutableStateOf(mainPrefs.getBoolean("default_yes", true)) }
    var newDeviceName by remember { mutableStateOf("") }

    var scheduleRules by remember {
        mutableStateOf(
            schedulePrefs.getStringSet("rules", emptySet())?.mapNotNull { stringToRule(it) }?.sortedBy { it.startTime } ?: listOf()
        )
    }

    LaunchedEffect(enableSound) {
        mainPrefs.edit().putBoolean("enable_sound", enableSound).apply()
    }
    LaunchedEffect(defaultYes) {
        mainPrefs.edit().putBoolean("default_yes", defaultYes).apply()
    }

    val turquoiseColor = Color(0xFF4DB6AC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
        ) {
            // --- Bluetooth Devices Section ---
            item {
                Text(
                    text = stringResource(R.string.bluetooth_devices_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(deviceNames.toList()) { device ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(device, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveDevice(device) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_device_description))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newDeviceName,
                        onValueChange = { newDeviceName = it },
                        label = { Text(stringResource(R.string.add_bluetooth_device_label)) },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newDeviceName.isNotBlank()) {
                                onAddDevice(newDeviceName)
                                newDeviceName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = turquoiseColor)
                    ) {
                        Text(stringResource(R.string.add_button))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Notification Sound Section ---
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_enable_sound), modifier = Modifier.weight(1f))
                    Switch(checked = enableSound, onCheckedChange = { enableSound = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Default Action Section ---
            item {
                Text(
                    text = stringResource(R.string.settings_default_action_prompt),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = defaultYes, onCheckedChange = { defaultYes = true })
                    Text(stringResource(R.string.notification_action_yes), fontSize = 8.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(checked = !defaultYes, onCheckedChange = { defaultYes = false })
                    Text(stringResource(R.string.notification_action_no), fontSize = 8.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
            }

            // --- Schedule Section (only when default is "No") ---
            if (!defaultYes) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.settings_scheduled_times_title),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(scheduleRules) { rule ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "${rule.days.joinToString(", ")}: ${rule.startTime} - ${rule.endTime}",
                            modifier = Modifier.weight(1f),
                            fontSize = 9.sp
                        )
                        IconButton(onClick = {
                            val updatedRules = scheduleRules.filter { it.id != rule.id }
                            val ruleStrings = updatedRules.map { r -> ruleToString(r) }.toSet()
                            schedulePrefs.edit().putStringSet("rules", ruleStrings).apply()
                            scheduleRules = updatedRules
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.settings_delete_rule))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAddSchedule,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_add_schedule))
                    }
                }
            }
        }
    }
}
