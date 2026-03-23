package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.RouteHazard
import com.example.pawtholepatrol.core.model.RoutePath
import com.example.pawtholepatrol.core.util.DistanceUtils
import com.example.pawtholepatrol.domain.repository.PotholeRepository
import com.example.pawtholepatrol.domain.repository.RoutingRepository
import kotlin.math.max

data class RouteHazardsResult(
    val route: RoutePath,
    val hazards: List<RouteHazard>,
)

class GetRouteHazardsUseCase(
    private val routingRepository: RoutingRepository,
    private val potholeRepository: PotholeRepository,
) {
    suspend operator fun invoke(
        origin: GeoPoint,
        destinationQuery: String,
        corridorMeters: Int = 60,
    ): RouteHazardsResult {
        val route = routingRepository.getRoute(origin, destinationQuery)
        val potholes = potholeRepository.getPotholesForRoute(route.points, corridorMeters)
        val hazards = potholes.map { pothole ->
            val closestDistance = route.points.minOfOrNull { point ->
                DistanceUtils.haversineMeters(point, pothole.location).toInt()
            } ?: Int.MAX_VALUE

            val distanceAlongRoute = max(closestDistance, 1)
            val etaSeconds = (distanceAlongRoute / 10.0).toInt()

            RouteHazard(
                pothole = pothole,
                distanceAlongRouteMeters = distanceAlongRoute,
                etaSeconds = etaSeconds,
            )
        }

        return RouteHazardsResult(route = route, hazards = hazards)
    }
}
