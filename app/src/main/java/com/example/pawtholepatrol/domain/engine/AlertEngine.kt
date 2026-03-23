package com.example.pawtholepatrol.domain.engine

import com.example.pawtholepatrol.core.model.DriveAlert
import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.RouteHazard
import com.example.pawtholepatrol.core.model.UserPrefs
import com.example.pawtholepatrol.core.util.DistanceUtils

class AlertEngine {
    fun buildAlerts(
        currentLocation: GeoPoint,
        hazards: List<RouteHazard>,
        prefs: UserPrefs,
    ): List<DriveAlert> {
        return hazards
            .map { hazard ->
                val directDistance = DistanceUtils.haversineMeters(currentLocation, hazard.pothole.location).toInt()
                hazard to directDistance
            }
            .filter { (_, directDistance) -> directDistance <= prefs.alertDistanceMeters }
            .sortedBy { (_, directDistance) -> directDistance }
            .map { (hazard, directDistance) ->
                DriveAlert(
                    message = "Pothole ahead in ${directDistance}m (${hazard.pothole.severity})",
                    potholeId = hazard.pothole.id,
                    distanceMeters = directDistance,
                    visual = true,
                    audio = prefs.soundEnabled || prefs.ttsEnabled,
                )
            }
    }
}
