package com.example.babyreminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyreminder.ui.theme.BabyReminderTheme

class MainActivity : AppCompatActivity() {

    private lateinit var drivingStateDetector: DrivingStateDetector
    private var isRecreating = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Permission result handled; receiver will work if granted.
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // After notification permission, check Bluetooth permission
        checkBluetoothPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drivingStateDetector = (application as BabyReminderApplication).drivingStateDetector
        checkNotificationPermission()

        // Skip splash screen if we're recreating after a language change
        val skipSplash = savedInstanceState != null || isRecreating
        val startRoute = if (skipSplash) "main" else "splash"

        enableEdgeToEdge()
        setContent {
            BabyReminderTheme {
                val navController = rememberNavController()
                val isDriving by drivingStateDetector.isDriving.collectAsState()
                val deviceNames by drivingStateDetector.carBluetoothDeviceNames.collectAsState()
                val currentLanguage = LocaleHelper.getLanguage(this)

                NavHost(navController = navController, startDestination = startRoute) {
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
                            onLanguageSelected = { lang ->
                                LocaleHelper.setLocale(this@MainActivity, lang)
                                // On API < 33, AppCompatDelegate doesn't auto-recreate
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    isRecreating = true
                                    this@MainActivity.recreate()
                                }
                            },
                            onNavigateToLegal = { navController.navigate("legal") },
                            onNavigateToSettings = { navController.navigate("settings") }
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
                            deviceNames = deviceNames,
                            onAddDevice = { drivingStateDetector.addCarBluetoothDevice(it) },
                            onRemoveDevice = { drivingStateDetector.removeCarBluetoothDevice(it) },
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

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkBluetoothPermission()
            }
        } else {
            checkBluetoothPermission()
        }
    }

    private fun checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}
