package com.example.pawtholepatrol.domain.repository

import com.example.pawtholepatrol.core.model.DriveSession
import com.example.pawtholepatrol.core.model.SensorSample
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    suspend fun startDriveSession(): DriveSession
    suspend fun stopDriveSession(sessionId: String)
    fun observeSensorSamples(sessionId: String): Flow<SensorSample>
}
