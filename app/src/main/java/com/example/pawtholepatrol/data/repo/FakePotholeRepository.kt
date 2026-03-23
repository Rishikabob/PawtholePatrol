package com.example.pawtholepatrol.data.repo

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.Pothole
import com.example.pawtholepatrol.core.model.PotholeSeverity
import com.example.pawtholepatrol.core.model.PotholeSource
import com.example.pawtholepatrol.core.util.DistanceUtils
import com.example.pawtholepatrol.domain.repository.PotholeRepository
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

class FakePotholeRepository : PotholeRepository {
    private val potholes = CopyOnWriteArrayList(
        listOf(
            seedPothole("nyc-001", 40.7582, -73.9853, PotholeSeverity.HIGH),
            seedPothole("nyc-002", 40.7575, -73.9868, PotholeSeverity.MEDIUM),
            seedPothole("nyc-003", 40.7601, -73.9839, PotholeSeverity.LOW),
            seedPothole("nyc-004", 40.7548, -73.9876, PotholeSeverity.HIGH),
            seedPothole("nyc-005", 40.7610, -73.9818, PotholeSeverity.MEDIUM),
        )
    )

    override suspend fun getNearbyPotholes(center: GeoPoint, radiusMeters: Int): List<Pothole> {
        val safeRadius = max(radiusMeters, 1)
        return potholes.filter { pothole ->
            DistanceUtils.haversineMeters(center, pothole.location) <= safeRadius
        }
    }

    override suspend fun getPotholesForRoute(routePoints: List<GeoPoint>, corridorMeters: Int): List<Pothole> {
        if (routePoints.isEmpty()) return emptyList()
        val safeCorridor = max(corridorMeters, 1)

        return potholes.filter { pothole ->
            routePoints.any { point ->
                DistanceUtils.haversineMeters(point, pothole.location) <= safeCorridor
            }
        }
    }

    override suspend fun submitDetectedPotholes(candidates: List<Pothole>) {
        potholes.addAll(candidates)
    }

    private fun seedPothole(id: String, lat: Double, lng: Double, severity: PotholeSeverity): Pothole {
        return Pothole(
            id = id,
            location = GeoPoint(latitude = lat, longitude = lng),
            severity = severity,
            confidence = 0.88f,
            lastVerifiedAtEpochMillis = System.currentTimeMillis(),
            source = PotholeSource.VERIFIED_DATASET,
        )
    }
}
