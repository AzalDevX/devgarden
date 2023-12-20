package com.azaldev.garden.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.azaldev.garden.MainActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.NotificationDuration
import com.azaldev.garden.globals.NotificationType
import com.azaldev.garden.globals.Notify
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.*

class LocationService() : Service() {
    private var currentLocation: Location? = null;
    private lateinit var lifecycleOwner: LifecycleOwner;
    private val job = SupervisorJob();
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var database: AppDatabase;
    private lateinit var gameDao: GameDao;

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location

            Log.d("devl|location", "Current location is x${location.latitude} y${location.longitude}")

//            notify.send("Current location is x${location.latitude} y${location.longitude}", NotificationType.INFO, NotificationDuration.TEMPORARY)

            sendLocationBroadcast(location);
            checkProximityToPointsOfInterest(location);
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.d("devl|location", "Location provider status changed: $status")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("devl|location", "Location provider enabled: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("devl|location", "Location provider disabled: $provider")
        }
    }

    private fun checkProximityToPointsOfInterest(location: Location) {
        scope.launch(Dispatchers.Default) {
            val gameList = gameDao.getGames()

            var distance: String = "";
            var game_name: String = "";

            Log.d("devl|location", "Updating the distance to ${gameList.size} games")

            for (game in gameList) {
                if (!game.isLocked) {
                    distance = Utilities.calculateDistance(game.x, game.y, location.latitude, location.longitude)
                        .toInt()
                        .toString() + "m";
                    game_name = game.name
                }
            }

            updateNotification("You are $distance from $game_name")
        }

        Log.d("devl|location", "Update notification executed")
    }

    private fun sendLocationBroadcast(location: Location) {
        val intent = Intent("com.azaldev.garden.LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground
        setupForegroundService()

        database = AppDatabase.getInstance(applicationContext)
        gameDao = database.GameDao();

        Log.i("devl|location", "Location service has been initialized!")

        checkLocationPermissionsAndStartService()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissionsAndStartService() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        /**
         * Check if the app has permission to get user precise location
         */
        if (areLocationPermissionsGranted()) {
            Log.i("devl|location", "GPS Provider is ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
            Log.i("devl|location", "Permissions have been granted, starting loc loop...")

            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastKnownLocation != null) {
                // Use the last known location as the initial location
                currentLocation = lastKnownLocation
                Log.d("devl|location", "Last known location: $currentLocation")
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                30000,  // 30 seconds
                0f,
                locationListener
            )
        } else {
            updateNotification("You must allow Location for this app to work!")

            // Schedule a delayed task to recheck permissions and restart the service
            Handler(Looper.getMainLooper()).postDelayed({
                checkLocationPermissionsAndStartService()
                Log.d("devl|location", "Checking location permissions again...")
            }, LOCATION_CHECK_INTERVAL.toLong())
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return (/*ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&*/
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(message: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val updatedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Game Service")
            .setContentText(message)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, updatedNotification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupForegroundService() {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Game Service")
            .setContentText("Getting closest game to player...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(NOTIFICATION_ID, notif)
        }

        startForeground(NOTIFICATION_ID, notif)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_NAME
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val LOCATION_CHECK_INTERVAL= 10 * 1000 // in ms
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val CHANNEL_NAME = "asdasdad"
    }
}
