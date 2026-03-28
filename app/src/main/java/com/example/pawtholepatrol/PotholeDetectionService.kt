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

class PotholeDetectionService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            Log.d("PotholeDetectionService", "Tracking active...")
            handler.postDelayed(this, 5_000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundServiceNotification()
            handler.post(runnable)
        } catch (securityException: SecurityException) {
            Log.e("PotholeDetectionService", "Unable to start foreground service; missing runtime permission or background eligibility", securityException)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START, null -> {
                val mode = intent?.getStringExtra(EXTRA_MODE)
                val trackingMode = if (mode == TrackingMode.AUTO.name) {
                    TrackingMode.AUTO
                } else {
                    TrackingMode.MANUAL
                }
                TrackingRuntime.onStarted(trackingMode)
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = CHANNEL_ID
        val channelName = "Pothole Detection Service"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pothole Detection Service")
            .setContentText("Tracking active in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        TrackingRuntime.onStopped()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.pawtholepatrol.action.START_TRACKING"
        const val ACTION_STOP = "com.example.pawtholepatrol.action.STOP_TRACKING"
        const val EXTRA_MODE = "mode"

        private const val CHANNEL_ID = "pothole_detection_channel"
        private const val NOTIFICATION_ID = 1
    }
}
