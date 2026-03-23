package com.example.pawtholepatrol.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.pawtholepatrol.R

class DriveMonitoringService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        ensureNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Drive monitoring active")
            .setContentText("Capturing accelerometer + GPS for pothole detection")
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // TODO: Wire sensor manager + fused location provider stream here.
        // TODO: Persist samples to local store and batch upload in worker.
    }

    private fun stopMonitoring() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Drive Monitoring",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.pawtholepatrol.action.START_MONITORING"
        const val ACTION_STOP = "com.example.pawtholepatrol.action.STOP_MONITORING"
        private const val CHANNEL_ID = "drive_monitoring"
        private const val NOTIFICATION_ID = 3001
    }
}
