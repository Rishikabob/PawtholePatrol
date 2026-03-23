package com.example.pawtholepatrol.data.repo

import com.example.pawtholepatrol.core.model.DriveSession
import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.SensorSample
import com.example.pawtholepatrol.domain.repository.SensorRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class FakeSensorRepository : SensorRepository {
    private val sessions = ConcurrentHashMap<String, DriveSession>()

    override suspend fun startDriveSession(): DriveSession {
        val session = DriveSession(
            id = UUID.randomUUID().toString(),
            startedAtEpochMillis = System.currentTimeMillis(),
        )
        sessions[session.id] = session
        return session
    }

    override suspend fun stopDriveSession(sessionId: String) {
        val existing = sessions[sessionId] ?: return
        sessions[sessionId] = existing.copy(endedAtEpochMillis = System.currentTimeMillis())
    }

    override fun observeSensorSamples(sessionId: String): Flow<SensorSample> = flow {
        while (currentCoroutineContext().isActive) {
            emit(
                SensorSample(
                    timestampEpochMillis = System.currentTimeMillis(),
                    location = GeoPoint(
                        latitude = 40.7580 + Random.nextDouble(-0.0015, 0.0015),
                        longitude = -73.9855 + Random.nextDouble(-0.0015, 0.0015),
                    ),
                    speedMps = Random.nextDouble(5.0, 18.0).toFloat(),
                    accelX = Random.nextDouble(-3.0, 3.0).toFloat(),
                    accelY = Random.nextDouble(-3.0, 3.0).toFloat(),
                    accelZ = Random.nextDouble(7.0, 14.5).toFloat(),
                )
            )
            delay(500)
        }
    }
}
