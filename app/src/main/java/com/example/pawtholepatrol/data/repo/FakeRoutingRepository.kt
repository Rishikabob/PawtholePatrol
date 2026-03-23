package com.example.pawtholepatrol.data.repo

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.RoutePath
import com.example.pawtholepatrol.domain.repository.RoutingRepository
import kotlin.math.abs

class FakeRoutingRepository : RoutingRepository {
    override suspend fun getRoute(origin: GeoPoint, destinationQuery: String): RoutePath {
        val routeLength = 24
        val destinationOffset = destinationQuery.hashCode().toDouble()
        val latDelta = (destinationOffset % 1000) / 800_000
        val lngDelta = (destinationOffset % 700) / 900_000

        val destination = GeoPoint(
            latitude = origin.latitude + latDelta,
            longitude = origin.longitude - abs(lngDelta),
        )

        val points = (0..routeLength).map { step ->
            val t = step.toDouble() / routeLength
            GeoPoint(
                latitude = origin.latitude + (destination.latitude - origin.latitude) * t,
                longitude = origin.longitude + (destination.longitude - origin.longitude) * t,
            )
        }

        return RoutePath(
            points = points,
            distanceMeters = 3_600,
            durationSeconds = 540,
        )
    }
}
