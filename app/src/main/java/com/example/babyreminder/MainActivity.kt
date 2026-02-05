package com.example.babyreminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyreminder.ui.theme.BabyReminderTheme

class MainActivity : ComponentActivity() {

    private lateinit var drivingStateDetector: DrivingStateDetector

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) { /* Permission granted, receiver will work. */ } else { /* Handle denial */ }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        checkBluetoothPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drivingStateDetector = (application as BabyReminderApplication).drivingStateDetector
        checkNotificationPermission()

        enableEdgeToEdge()
        setContent {
            BabyReminderTheme {
                val navController = rememberNavController()
                val isDriving by drivingStateDetector.isDriving.collectAsState()
                val deviceNames by drivingStateDetector.carBluetoothDeviceNames.collectAsState()
                val currentLanguage = LocaleHelper.getLanguage(this)

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(
                            onSplashComplete = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            isDriving = isDriving,
                            currentLanguageCode = currentLanguage ?: "en",
                            onLanguageSelected = {
                                LocaleHelper.setLocale(this@MainActivity, it)
                                this@MainActivity.recreate()
                            },
                            onNavigateToBluetoothSettings = { navController.navigate("bluetooth") },
                            onNavigateToLegal = { navController.navigate("legal") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("bluetooth") {
                        BluetoothDevicesScreen(
                            deviceNames = deviceNames,
                            onAddDevice = { drivingStateDetector.addCarBluetoothDevice(it) },
                            onRemoveDevice = { drivingStateDetector.removeCarBluetoothDevice(it) },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("legal") {
                        LegalScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToTerms = { navController.navigate("terms") },
                            onNavigateToPrivacy = { navController.navigate("privacy") },
                            onNavigateToDisclaimer = { navController.navigate("disclaimer") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToAddSchedule = { navController.navigate("schedule") }
                        )
                    }
                    composable("schedule") {
                        ScheduleScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("terms") {
                        TermsOfUseScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("privacy") {
                        PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("disclaimer") {
                        DisclaimerScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    private fun checkBluetoothPermission() {
        // ... (permission checking logic is unchanged)
    }

    private fun checkNotificationPermission() {
        // ... (permission checking logic is unchanged)
    }
}
