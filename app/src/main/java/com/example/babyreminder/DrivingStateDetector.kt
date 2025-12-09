package com.example.babyreminder

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DrivingStateDetector(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("bluetooth_devices_prefs", Context.MODE_PRIVATE)

    // This now represents the logical driving state, updated by the BroadcastReceiver
    private val _isDriving = MutableStateFlow(false)
    val isDriving: StateFlow<Boolean> = _isDriving.asStateFlow()

    private val _carBluetoothDeviceNames = MutableStateFlow(emptySet<String>())
    val carBluetoothDeviceNames: StateFlow<Set<String>> = _carBluetoothDeviceNames.asStateFlow()

    init {
        _carBluetoothDeviceNames.value = prefs.getStringSet("device_names", emptySet()) ?: emptySet()
        // Reflect the persisted driving state on init
        val drivingPrefs = context.getSharedPreferences("driving_prefs", Context.MODE_PRIVATE)
        _isDriving.value = drivingPrefs.getBoolean("is_driving_physical", false)

    }

    fun addCarBluetoothDevice(deviceName: String) {
        _carBluetoothDeviceNames.update { it + deviceName }
        saveDeviceNames()
    }

    fun removeCarBluetoothDevice(deviceName: String) {
        _carBluetoothDeviceNames.update { it - deviceName }
        saveDeviceNames()
    }

    private fun saveDeviceNames() {
        prefs.edit().putStringSet("device_names", _carBluetoothDeviceNames.value).apply()
    }

    // This can be called by the broadcast receiver to update the state
    fun setDrivingState(isDriving: Boolean, context: Context) {
        _isDriving.value = isDriving
        val drivingPrefs = context.getSharedPreferences("driving_prefs", Context.MODE_PRIVATE)
        drivingPrefs.edit().putBoolean("is_driving_physical", isDriving).apply()
    }
}
