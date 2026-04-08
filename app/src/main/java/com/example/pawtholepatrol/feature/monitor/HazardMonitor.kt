package com.example.pawtholepatrol.feature.monitor

import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.notifications.NotificationHelper

class HazardMonitor(
    private val index: GeoSpatialIndex,
    private val radiusMeters: Double,
    private val notificationHelper: NotificationHelper,
    private val onEvent: (String) -> Unit,
    private val onHazardDetected: (GeoPoint) -> Unit = {},
) {

    private var isInsideHazardZone = false

    fun onLocationUpdate(current: GeoPoint) {
        val nearby = index.findNearby(current, radiusMeters)
        val currentlyInside = nearby.isNotEmpty()

        println("Location: $current | Nearby: ${nearby.size}")

        if (!isInsideHazardZone && currentlyInside) {
            isInsideHazardZone = true

            notificationHelper.showCriticalNotification(
                "Hazard Ahead",
                "You are entering a hazard area"
            )

            onEvent("ENTER hazard zone")
            onHazardDetected(nearby.first())
        }
        else if (isInsideHazardZone && !currentlyInside) {
            isInsideHazardZone = false

            notificationHelper.showCriticalNotification(
                "Safe Area",
                "You have left the hazard area"
            )

            onEvent("EXIT hazard zone")
        }
    }
}
