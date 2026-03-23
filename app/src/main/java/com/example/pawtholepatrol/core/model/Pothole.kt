package com.example.pawtholepatrol.core.model

enum class PotholeSeverity {
    LOW,
    MEDIUM,
    HIGH,
}

enum class PotholeSource {
    CROWDSOURCED,
    SENSOR,
    VERIFIED_DATASET,
}

data class Pothole(
    val id: String,
    val location: GeoPoint,
    val severity: PotholeSeverity,
    val confidence: Float,
    val lastVerifiedAtEpochMillis: Long,
    val source: PotholeSource,
)
