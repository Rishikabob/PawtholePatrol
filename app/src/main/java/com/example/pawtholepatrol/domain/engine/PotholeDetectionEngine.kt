package com.example.pawtholepatrol.domain.engine

import com.example.pawtholepatrol.core.model.Pothole
import com.example.pawtholepatrol.core.model.PotholeSeverity
import com.example.pawtholepatrol.core.model.PotholeSource
import com.example.pawtholepatrol.core.model.SensorSample
import java.util.UUID
import kotlin.math.sqrt

class PotholeDetectionEngine {
    fun detectCandidates(samples: List<SensorSample>, sensitivity: Float): List<Pothole> {
        if (samples.isEmpty()) return emptyList()

        val threshold = 11.5f - (sensitivity.coerceIn(0f, 1f) * 2.0f)
        return samples.mapNotNull { sample ->
            val magnitude = sqrt(
                (sample.accelX * sample.accelX) +
                    (sample.accelY * sample.accelY) +
                    (sample.accelZ * sample.accelZ)
            )

            if (magnitude < threshold) {
                null
            } else {
                val severity = when {
                    magnitude > 16f -> PotholeSeverity.HIGH
                    magnitude > 13.5f -> PotholeSeverity.MEDIUM
                    else -> PotholeSeverity.LOW
                }
                Pothole(
                    id = "candidate-${UUID.randomUUID()}",
                    location = sample.location,
                    severity = severity,
                    confidence = 0.6f,
                    lastVerifiedAtEpochMillis = sample.timestampEpochMillis,
                    source = PotholeSource.SENSOR,
                )
            }
        }
    }
}
