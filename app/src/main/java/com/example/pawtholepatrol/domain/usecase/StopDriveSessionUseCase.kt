package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.domain.repository.SensorRepository

class StopDriveSessionUseCase(
    private val sensorRepository: SensorRepository,
) {
    suspend operator fun invoke(sessionId: String) {
        sensorRepository.stopDriveSession(sessionId)
    }
}
