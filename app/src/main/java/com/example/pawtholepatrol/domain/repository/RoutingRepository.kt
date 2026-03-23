package com.example.pawtholepatrol.domain.repository

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.RoutePath

interface RoutingRepository {
    suspend fun getRoute(origin: GeoPoint, destinationQuery: String): RoutePath
}
