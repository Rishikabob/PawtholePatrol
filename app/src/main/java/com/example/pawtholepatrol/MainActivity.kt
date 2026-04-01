package com.example.pawtholepatrol

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.example.pawtholepatrol.feature.root.PawtholeApp
import com.example.pawtholepatrol.ui.theme.PawtholePatrolTheme
import com.example.pawtholepatrol.utility.ActivityTransitionUtil
import com.example.pawtholepatrol.utility.EventConfirmationHelper
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class MainActivity : ComponentActivity() {

    private lateinit var client: ActivityRecognitionClient

    private lateinit var notificationHelper: NotificationHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawtholePatrolTheme {
                PawtholeApp()
            }
        }

        client = ActivityRecognition.getClient(this)
        requestStartupPermissions()
        EventConfirmationHelper.init(this)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createChannels()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("ActivityRecognition", "Permission Granted")
                startActivityUpdates()
            } else {
                requestActivityPermission()
            }
        }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            requestActivityPermission()
        }

    private fun requestStartupPermissions() {
        val missing = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            missing += Manifest.permission.POST_NOTIFICATIONS
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missing += Manifest.permission.ACCESS_FINE_LOCATION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            missing += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }

        if (missing.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(missing.toTypedArray())
        } else {
            requestActivityPermission()
        }
    }

    private fun requestActivityPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ActivityRecognition", "Asking for permission")
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            Log.d("ActivityRecognition", "Permission Granted")
            startActivityUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startActivityUpdates() {
        client
            .requestActivityTransitionUpdates(
                ActivityTransitionUtil.getActivityTransitionRequest(),
                getPendingIntent()
            )
            .addOnSuccessListener { Log.d("ActivityRecognition", "Activity updates requested successfully") }
            .addOnFailureListener { Log.e("ActivityRecognition", "Activity updates failed", it) }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityReceiver::class.java)
        return PendingIntent.getBroadcast(this, 122, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Close the inquiry utility on application shutdown
        EventConfirmationHelper.shutdown()
    }
}
