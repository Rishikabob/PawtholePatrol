package com.example.pawtholepatrol.feature.monitor

import android.content.Context
import android.util.Log
import com.example.pawtholepatrol.AppPreferences
import com.example.pawtholepatrol.PotholeCsvStore
import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.example.pawtholepatrol.utility.EventConfirmationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HazardMonitor(
    private val context: Context,
    private val index: GeoSpatialIndex,
    private val radiusMeters: Double,
    private val notificationHelper: NotificationHelper,
    private val onEvent: (String) -> Unit,
    private val onHazardDetected: (GeoPoint) -> Unit = {},
) {

    private var isInsideHazardZone = false
    private var lastDetectedPoint: GeoPoint? = null // Track the point that triggered the alert
    private val scope = CoroutineScope(Dispatchers.Main)

    fun onLocationUpdate(current: GeoPoint) {
        val nearby = index.findNearby(current, radiusMeters)
        val currentlyInside = nearby.isNotEmpty()

        println("Location: $current | Nearby: ${nearby.size}")

        if (!isInsideHazardZone && currentlyInside) {
            isInsideHazardZone = true
            lastDetectedPoint = nearby.first()  // Save the triggering geo point

            notificationHelper.showCriticalNotification(
                "Hazard Ahead",
                "You are entering a hazard area"
            )

            onEvent("ENTER hazard zone")
            onHazardDetected(nearby.first())
        }
        else if (isInsideHazardZone && !currentlyInside) {
            isInsideHazardZone = false
            val pointToDelete = lastDetectedPoint   // Capture before clearing

            // If the user allows inquiries, after the hazard area ask if a pothole was present
            if (AppPreferences.isInquiryEnabled(context)) {
                EventConfirmationHelper.askForConfirmation(
                    context,
                    "Was there a pothole?",
                    { confirmed ->
                        if (confirmed) {
                            Log.d("Visual Notification", "User confirmed the event or time out")
                        } else {
                            Log.d("Visual Notification", "User denied pothole, delete point")

                            // Delete the point from the dataset
                            pointToDelete?.let { point ->
                                scope.launch {
                                    val updated = PotholeCsvStore.readAll(context)
                                        .toMutableList()
                                        .apply { remove(point) }
                                    PotholeCsvStore.replaceAll(context, updated)

                                    // Rebuild the index so it stops triggering with this geo point
                                    index.rebuild(updated)
                                }
                            }
                        }
                    }
                )
            } else {
                // If inquiries are turned off, only inform user that they have left the hazard area
                notificationHelper.showCriticalNotification(
                    "Safe Area",
                    "You have left the hazard area"
                )
            }

            lastDetectedPoint = null
            onEvent("EXIT hazard zone")
        }
    }
}
