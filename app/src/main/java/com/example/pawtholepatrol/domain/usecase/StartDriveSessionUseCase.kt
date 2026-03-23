package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.core.model.DriveSession
import com.example.pawtholepatrol.domain.repository.SensorRepository

class StartDriveSessionUseCase(
    private val sensorRepository: SensorRepository,
) {
    suspend operator fun invoke(): DriveSession = sensorRepository.startDriveSession()
}
