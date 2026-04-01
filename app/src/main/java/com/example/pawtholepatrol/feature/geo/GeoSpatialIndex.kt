package com.example.pawtholepatrol.feature.geo

import kotlin.math.cos

class GeoSpatialIndex(
    private val cellSizeMeters: Double = 100.0
) {

    private val metersPerDegree = 111_000.0

    private val grid = mutableMapOf<Pair<Int, Int>, MutableList<GeoPoint>>()

    private fun getCell(point: GeoPoint): Pair<Int, Int> {
        val latMeters = point.latitude * metersPerDegree
        val lonMeters = point.longitude * metersPerDegree *
                cos(Math.toRadians(point.latitude))

        val x = (latMeters / cellSizeMeters).toInt()
        val y = (lonMeters / cellSizeMeters).toInt()

        return Pair(x, y)
    }

    fun addPoint(point: GeoPoint) {
        val cell = getCell(point)
        grid.getOrPut(cell) { mutableListOf() }.add(point)
    }

    fun addPoints(points: List<GeoPoint>) {
        points.forEach { addPoint(it) }
    }

    fun findNearby(
        current: GeoPoint,
        radiusMeters: Double
    ): List<GeoPoint> {

        val result = mutableListOf<GeoPoint>()

        val currentCell = getCell(current)
        val radiusSquared = radiusMeters * radiusMeters
        
        val currentLatRad = Math.toRadians(current.latitude)
        val cosLat = cos(currentLatRad)

        val searchRadius = (radiusMeters / cellSizeMeters).toInt() + 1

        for (dx in -searchRadius..searchRadius) {
            for (dy in -searchRadius..searchRadius) {

                val neighborCell = Pair(
                    currentCell.first + dx,
                    currentCell.second + dy
                )

                val points = grid[neighborCell] ?: continue

                for (point in points) {

                    val dxMeters = (point.longitude - current.longitude) *
                            metersPerDegree * cosLat

                    val dyMeters = (point.latitude - current.latitude) *
                            metersPerDegree

                    val distanceSquared =
                        dxMeters * dxMeters + dyMeters * dyMeters

                    if (distanceSquared <= radiusSquared) {
                        result.add(point)
                    }
                }
            }
        }

        return result
    }
}