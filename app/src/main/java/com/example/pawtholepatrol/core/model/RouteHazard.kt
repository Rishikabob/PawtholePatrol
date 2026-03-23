package com.example.pawtholepatrol.core.model

data class RouteHazard(
    val pothole: Pothole,
    val distanceAlongRouteMeters: Int,
    val etaSeconds: Int,
)
