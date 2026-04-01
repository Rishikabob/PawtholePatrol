package com.example.pawtholepatrol.feature.geo

import kotlin.math.cos
import kotlin.math.sqrt

object GeoUtils {

    private const val METERS_PER_DEGREE_LAT = 111_000.0

    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val avgLat = Math.toRadians((a.latitude + b.latitude) / 2.0)

        val dx = (b.longitude - a.longitude) * METERS_PER_DEGREE_LAT * cos(avgLat)
        val dy = (b.latitude - a.latitude) * METERS_PER_DEGREE_LAT

        return sqrt(dx * dx + dy * dy)
    }
}