package com.azaldev.garden.globals

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat

enum class NotificationType {
    INFO,
    WARNING,
    ERROR
}

enum class NotificationDuration {
    TEMPORARY,
    PERMANENT
}

class Notify(private val context: Context) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannels()
        requestPermission()
    }

    companion object {
        private var instance: Notify? = null

        fun getInstance(context: Context): Notify {
            if (instance == null) {
                instance = Notify(context.applicationContext)
            }
            return instance!!
        }
    }

    private fun createNotificationChannels() {
        createNotificationChannel("info_channel", "Info Channel")
        createNotificationChannel("warning_channel", "Warning Channel")
        createNotificationChannel("error_channel", "Error Channel")
    }

    fun send(message: String, type: NotificationType, duration: NotificationDuration) {
        if (!hasPermission()) {
            requestPermission()
            return
        }

        when (type) {
            NotificationType.INFO -> sendNotification(
                message,
                duration,
                "Info Notification",
                android.R.drawable.ic_dialog_info,
                "info_channel"
            )
            NotificationType.WARNING -> sendNotification(
                message,
                duration,
                "Warning Notification",
                android.R.drawable.ic_dialog_alert,
                "warning_channel"
            )
            NotificationType.ERROR -> sendNotification(
                message,
                duration,
                "Error Notification",
                android.R.drawable.stat_notify_error,
                "error_channel"
            )
        }
    }

    private fun sendNotification(
        message: String,
        duration: NotificationDuration,
        title: String,
        icon: Int,
        channelId: String
    ) {
        val notificationId = title.hashCode()

        val notificationBuilder = Notification.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)

        if (duration == NotificationDuration.PERMANENT) {
            notificationBuilder.setOngoing(true)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName ?: "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasPermission(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    private fun requestPermissionIfRequired() {
        if (!hasPermission()) {
            requestPermission()
        }
    }

    private fun requestPermission() {
//        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }
}
