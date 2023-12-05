package com.azaldev.garden.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat

class LocationService : Service() {

    // Define your global variable to store the location
    private var currentLocation: Location? = null

    // Define a location listener to update the currentLocation
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            // Add logic to check proximity to points of interest
            checkProximityToPointsOfInterest(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
//        override fun onProviderEnabled(provider: String?) {}
//        override fun onProviderDisabled(provider: String?) {}
    }

    // Add logic to check proximity to points of interest
    private fun checkProximityToPointsOfInterest(location: Location) {
        // Implement logic to check proximity and print logs
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Request location updates every 30 seconds
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        /**
         * Check if the app has permission to get user precise location
         */
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                30000,  // 30 seconds
                0f,
                locationListener
            )
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
