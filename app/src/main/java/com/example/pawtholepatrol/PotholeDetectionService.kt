package com.example.pawtholepatrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class PotholeDetectionService: Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            Log.d("PotholeDetectionService", "Hello")
            handler.postDelayed(this, 5000) // repeat every 5 seconds
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        handler.post(runnable)
    }

    private fun startForegroundServiceNotification() {
        val channelId = "pothole_detection_channel"
        val channelName = "Pothole Detection Service"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pothole Detection Service")
            .setContentText("Checking for nearby potholes")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // use your icon
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}