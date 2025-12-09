package com.example.babyreminder

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

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
fun SettingsScreen(onNavigateBack: () -> Unit, onNavigateToAddSchedule: () -> Unit) {
    val context = LocalContext.current
    val mainPrefs = remember { context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE) }
    val schedulePrefs = remember { context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE) }

    var enableSound by remember { mutableStateOf(mainPrefs.getBoolean("enable_sound", true)) }
    var defaultYes by remember { mutableStateOf(mainPrefs.getBoolean("default_yes", true)) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Notification Sounds", modifier = Modifier.weight(1f))
                Switch(checked = enableSound, onCheckedChange = { enableSound = it })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What would you like to be the default action if you don\'t respond to the first alert?",
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = defaultYes, onCheckedChange = { defaultYes = true })
                Text("Yes, the children are in the car", fontSize = 8.sp)
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(checked = !defaultYes, onCheckedChange = { defaultYes = false })
                Text("No", fontSize = 8.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            AnimatedVisibility(visible = !defaultYes) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scheduled Times for \"Yes\"",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
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
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onNavigateToAddSchedule, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Add Scheduled Time")
                    }
                }
            }
        }
    }
}
