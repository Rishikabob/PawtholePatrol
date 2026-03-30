package com.example.pawtholepatrol.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val ALERTS_CHANNEL_ID = "alerts_channel_v1"
    private val CRITICAL_CHANNEL_ID = "critical_channel_v1"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alertsChannel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            "General Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val criticalChannel = NotificationChannel(
            CRITICAL_CHANNEL_ID,
            "Critical Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )

        manager.createNotificationChannel(alertsChannel)
        manager.createNotificationChannel(criticalChannel)
    }

    fun showCriticalNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CRITICAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showGeneralNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}