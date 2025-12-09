package com.example.babyreminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDevicesScreen(
    deviceNames: Set<String>,
    onAddDevice: (String) -> Unit,
    onRemoveDevice: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newDeviceName by remember { mutableStateOf("") }
    val turquoiseColor = Color(0xFF4DB6AC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.bluetooth_devices_title), fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                }
            )
        }
    ) {
        Column(modifier = modifier.padding(it).padding(16.dp)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(deviceNames.toList()) { deviceName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(deviceName, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveDevice(deviceName) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.remove_device_description))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = newDeviceName,
                    onValueChange = { newDeviceName = it },
                    label = { Text(stringResource(id = R.string.add_bluetooth_device_label)) },
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
                    Text(stringResource(id = R.string.add_button))
                }
            }
        }
    }
}
