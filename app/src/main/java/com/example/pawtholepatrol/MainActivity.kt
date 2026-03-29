package com.example.pawtholepatrol

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.example.pawtholepatrol.feature.root.PawtholeApp
import com.example.pawtholepatrol.ui.theme.PawtholePatrolTheme

class MainActivity : ComponentActivity() {
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

        notificationHelper = NotificationHelper(this)
        notificationHelper.createChannel()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }
}
