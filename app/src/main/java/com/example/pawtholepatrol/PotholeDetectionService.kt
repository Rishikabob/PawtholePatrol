package com.example.pawtholepatrol

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.monitor.HazardMonitor
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class PotholeDetectionService : Service() {
    private var geoPointList: List<GeoPoint> = emptyList()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var hazardMonitor: HazardMonitor

    override fun onCreate() {
        super.onCreate()
        try {
            geoPointList = runBlocking(Dispatchers.IO) {
                PotholeCsvStore.readAll(this@PotholeDetectionService)
            }
            Log.d("PawtholePatrolLogs", "Found ${geoPointList.size} potholes in writable csv")

            val notificationHelper = NotificationHelper(this)
            val hazardIndex = GeoSpatialIndex()
            hazardIndex.addPoints(geoPointList)
            hazardMonitor = HazardMonitor(
                index = hazardIndex,
                radiusMeters = 100.0, // can read from settings, or leave hard coded for test/demo
                notificationHelper = notificationHelper, // can modify/remove if other notification source is preferred
                onEvent = { message ->
                    // in case we want to respond to enter/exit hazard zone event
                },
                onHazardDetected = { point ->
                    // in case we want to respond using the location of a hazard
                },
            )

            startForegroundServiceNotification()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000 // 5 seconds
            )
                .setMinUpdateDistanceMeters(10f) // only trigger if moved 10m
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {
                        checkLocation(location.latitude, location.longitude)
                    }
                }
            }

            startLocationUpdates(locationRequest)
        } catch (securityException: SecurityException) {
            Log.e("PotholeDetectionService", "Unable to start foreground service; missing runtime permission or background eligibility", securityException)
            stopSelf()
        }
    }

    private fun checkLocation(lat: Double, lon: Double) {
        Log.d("PawtholePatrolLogs", "Checking location for Lat: $lat, Lon: $lon")

        // 🔥 Your pothole logic here
        hazardMonitor.onLocationUpdate(GeoPoint(lat, lon))
    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
