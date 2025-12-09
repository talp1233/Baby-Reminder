package com.example.babyreminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDriving: Boolean,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onNavigateToBluetoothSettings: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val turquoiseColor = Color(0xFF4DB6AC)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.driving_status),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(if (isDriving) Color.Yellow else Color.Green),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDriving) stringResource(id = R.string.driving_status_active) else stringResource(id = R.string.driving_status_inactive),
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.bluetooth_list_prompt),
                    textAlign = TextAlign.Center,
                    fontSize = 8.sp,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToBluetoothSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = turquoiseColor)
                ) {
                    Text(stringResource(id = R.string.bluetooth_list_button), fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToLegal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = turquoiseColor)
                ) {
                    Text(stringResource(id = R.string.legal_button), fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = turquoiseColor)
                ) {
                    Text("Settings", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.contact_us))
                    Text(
                        text = "BabyReminder@outlook.com",
                        modifier = Modifier.clickable { uriHandler.openUri("mailto:BabyReminder@outlook.com") },
                        fontSize = 14.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = stringResource(id = R.string.select_language),
                        modifier = Modifier.size(32.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    val languages = mapOf("English" to "en", "עברית" to "he", "العربية" to "ar", "Русский" to "ru", "Español" to "es")
                    languages.forEach { (name, code) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { 
                                onLanguageSelected(code)
                                menuExpanded = false 
                            },
                            trailingIcon = if (currentLanguageCode == code) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                            } else null
                        )
                    }
                }
            }
        }
    }
}
