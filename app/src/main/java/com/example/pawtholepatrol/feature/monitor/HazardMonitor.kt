package com.example.pawtholepatrol.feature.monitor

import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.notifications.NotificationHelper

class HazardMonitor(
    private val index: GeoSpatialIndex,
    private val radiusMeters: Double,
    private val notificationHelper: NotificationHelper
) {
    private var isInsideHazardZone = false

    fun onLocationUpdate(current: GeoPoint) {
        val nearby = index.findNearby(current, radiusMeters)

        val currentlyInside = nearby.isNotEmpty()

        if (!isInsideHazardZone && currentlyInside) {
            isInsideHazardZone = true

            notificationHelper.showGeneralNotification(
                "Hazard Ahead",
                "You are entering a hazard area"
            )

            println("ENTER hazard zone")
        }
        else if (isInsideHazardZone && !currentlyInside) {
            isInsideHazardZone = false

            notificationHelper.showGeneralNotification(
                "Safe Area",
                "You have left the hazard area"
            )

            println("EXIT hazard zone")
        }
    }
}