package com.example.pawtholepatrol.core.model

data class SensorSample(
    val timestampEpochMillis: Long,
    val location: GeoPoint,
    val speedMps: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
)
