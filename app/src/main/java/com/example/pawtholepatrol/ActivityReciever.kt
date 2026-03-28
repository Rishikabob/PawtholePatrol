package com.example.pawtholepatrol

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.pawtholepatrol.utility.ActivityTransitionUtil
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActivityRecognition", "Received intent: $intent, extras=${intent.extras?.keySet()}")
        if (!ActivityTransitionResult.hasResult(intent)) return

        val result = ActivityTransitionResult.extractResult(intent) ?: return
        result.transitionEvents.forEach { event ->
            val info = "Transition: ${ActivityTransitionUtil.toActivityString(event.activityType)}  - ${ActivityTransitionUtil.toTransitionType(event.transitionType)}"
            Log.d("ActivityRecognition", info)

            if (event.activityType == DetectedActivity.IN_VEHICLE && !AppPreferences.isAutoDetectEnabled(context)) {
                Log.d("ActivityRecognition", "Auto Detect disabled in settings; ignoring transition")
                return@forEach
            }

            if (event.activityType == DetectedActivity.IN_VEHICLE && event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                val hasFineLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
                val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                if (!hasFineLocation || !hasBackgroundLocation) {
                    Log.w("ActivityRecognition", "Skipping auto-start; location/background location permission missing")
                    return@forEach
                }

                Log.d("ActivityRecognition", "In vehicle: starting pothole detection service")
                val serviceIntent = Intent(context, PotholeDetectionService::class.java).apply {
                    action = PotholeDetectionService.ACTION_START
                    putExtra(PotholeDetectionService.EXTRA_MODE, TrackingMode.AUTO.name)
                }
                context.startForegroundService(serviceIntent)
            } else if (event.activityType == DetectedActivity.IN_VEHICLE && event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                Log.d("ActivityRecognition", "Vehicle exit: stopping pothole detection service")
                val serviceIntent = Intent(context, PotholeDetectionService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }
}
