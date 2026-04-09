package com.example.pawtholepatrol

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
            requestStartupPermissions()
        }

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            startActivityUpdates()
        }

    private fun requestStartupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs notification access.")
                .setPositiveButton("Continue") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                .setCancelable(false) // disables back button dismiss
                .show()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs fine location access.")
                .setPositiveButton("Continue") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                .setCancelable(false) // disables back button dismiss
                .show()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs background location access. Select \"Allow all the time\".")
                .setPositiveButton("Continue") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .setCancelable(false) // disables back button dismiss
                .show()
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs activity recognition access.")
                .setPositiveButton("Continue") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
                .setCancelable(false) // disables back button dismiss
                .show()
        } else if (!Settings.canDrawOverlays(this)) {
            Log.d("PawtholePatrolLogs", "Asking for overlay permission")
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs overlay access. Select \"Pawthole Patrol\" and toggle \"Allow\".")
                .setPositiveButton("Continue") { _, _ ->
                    overlayPermissionLauncher.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                }
                .setCancelable(false) // disables back button dismiss
                .show()
        } else {
            startActivityUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startActivityUpdates() {
        Log.d("PawtholePatrolLogs", "Permission Granted")
        client
            .requestActivityTransitionUpdates(
                ActivityTransitionUtil.getActivityTransitionRequest(),
                getPendingIntent()
            )
            .addOnSuccessListener { Log.d("PawtholePatrolLogs", "Activity updates requested successfully") }
            .addOnFailureListener { Log.e("PawtholePatrolLogs", "Activity updates failed", it) }
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
