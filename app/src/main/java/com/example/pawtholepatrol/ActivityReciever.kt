package com.example.pawtholepatrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pawtholepatrol.utility.ActivityTransitionUtil
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActivityRecognition", "Received intent: $intent, extras=${intent.extras?.keySet()}")
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                result.transitionEvents.forEach { event ->
                    val info = "Transition: ${ActivityTransitionUtil.toActivityString(event.activityType)}  - ${ActivityTransitionUtil.toTransitionType(event.transitionType)}"
                    Log.d("ActivityRecognition", info)

                    if (event.activityType == DetectedActivity.IN_VEHICLE && event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        Log.d("ActivityRecognition", "In vehicle: starting pothole detection service")
                        val serviceIntent = Intent(context, PotholeDetectionService::class.java)
                        context.startForegroundService(serviceIntent)
                    } else {
                        Log.d("ActivityRecognition", "Not in vehicle: stopping pothole detection service")
                        val serviceIntent = Intent(context, PotholeDetectionService::class.java)
                        context.stopService(serviceIntent)
                    }
                }
            }
        }
    }
}