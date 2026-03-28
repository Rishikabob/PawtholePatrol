package com.example.pawtholepatrol

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pawtholepatrol.feature.root.PawtholeApp
import com.example.pawtholepatrol.ui.theme.PawtholePatrolTheme
import com.example.pawtholepatrol.util.ActivityTransitionUtil
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class MainActivity : ComponentActivity() {

    private lateinit var client: ActivityRecognitionClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawtholePatrolTheme {
                PawtholeApp()
            }
        }
        client = ActivityRecognition.getClient(this)
        requestActivityPermission()
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
}
