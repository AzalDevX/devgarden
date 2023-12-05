package com.azaldev.garden.globals

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.azaldev.garden.services.LocationService

object LocationServiceManager {

    private var isServiceRunning = false

    fun startLocationService(context: Context) {
        if (!isServiceRunning) {
            val serviceIntent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            isServiceRunning = true
        }
    }

    fun stopLocationService(context: Context) {
        if (isServiceRunning) {
            val serviceIntent = Intent(context, LocationService::class.java)
            context.stopService(serviceIntent)
            isServiceRunning = false
        }
    }

    fun emulateLocationUpdate(context: Context) {
        if (isServiceRunning) {
            stopLocationService(context);
        }

    }
}
