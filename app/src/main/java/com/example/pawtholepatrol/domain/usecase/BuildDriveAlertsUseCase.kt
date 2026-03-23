package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.core.model.DriveAlert
import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.RouteHazard
import com.example.pawtholepatrol.domain.engine.AlertEngine
import com.example.pawtholepatrol.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first

class BuildDriveAlertsUseCase(
    private val alertEngine: AlertEngine,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(currentLocation: GeoPoint, hazards: List<RouteHazard>): List<DriveAlert> {
        val prefs = preferencesRepository.observeUserPrefs().first()
        return alertEngine.buildAlerts(
            currentLocation = currentLocation,
            hazards = hazards,
            prefs = prefs,
        )
    }
}
