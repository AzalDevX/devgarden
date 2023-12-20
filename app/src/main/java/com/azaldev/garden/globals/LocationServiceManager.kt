package com.azaldev.garden.globals

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo.PROTECTION_NORMAL
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.azaldev.garden.services.LocationService

object LocationServiceManager {

    private var isServiceRunning = false

    fun startLocationService(context: Context, activity: Activity, lifecycleOwner: LifecycleOwner) {
        if (!isServiceRunning) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PROTECTION_NORMAL
                )

            Log.d("devl|service", "Starting location service...")
            val serviceIntent = Intent(context, LocationService::class.java)
//            serviceIntent.putExtra("lifecycleOwner", lifecycleOwner)
            ContextCompat.startForegroundService(context, serviceIntent)
            isServiceRunning = true
        }
    }

    fun stopLocationService(context: Context) {
        if (isServiceRunning) {
            Log.d("devl|service", "Stopping location service...")
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
