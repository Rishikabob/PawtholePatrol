package com.example.pawtholepatrol.domain.repository

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.Pothole

interface PotholeRepository {
    suspend fun getNearbyPotholes(center: GeoPoint, radiusMeters: Int): List<Pothole>
    suspend fun getPotholesForRoute(routePoints: List<GeoPoint>, corridorMeters: Int): List<Pothole>
    suspend fun submitDetectedPotholes(candidates: List<Pothole>)
}
