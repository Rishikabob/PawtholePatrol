package com.example.pawtholepatrol.core.model

data class RoutePath(
    val points: List<GeoPoint>,
    val distanceMeters: Int,
    val durationSeconds: Int,
)
