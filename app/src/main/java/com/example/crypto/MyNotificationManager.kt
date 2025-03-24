package com.example.crypto

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class MyNotificationManager(private val context: Context) {

    private val CHANNEL_ID = "my_channel_id"

    init {
        createNotificationChannel()
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        Log.d("NOTIFICATION", "Supposed to create notification channel")
        val channelName = "My Channel"
        val channelDescription = "Channel for My App Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("NotificationPermission")
    fun sendNotification(title: String, message: String) {
        Log.d("NOTIFICATION", "Supposed to send notification")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sprite_0002)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, notificationBuilder.build())

    }
}