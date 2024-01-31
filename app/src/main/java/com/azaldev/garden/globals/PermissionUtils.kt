package com.azaldev.garden.globals

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    const val CAMERA_PERMISSION_REQUEST_CODE = 100
    const val LOCATION_PERMISSION_REQUEST_CODE = 101
    const val BG_LOCATION_PERMISSION_REQUEST_CODE = 102

    fun checkAndRequestCameraPermission(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return false;
        } else return true;
    }

    fun  checkAndRequestLocationPermission(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED /*||
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED*/
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION // No need
                    //                     android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, // It has to be requested in a separate page
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BG_LOCATION_PERMISSION_REQUEST_CODE
            )
            return false;
        } else return true;
    }
}
