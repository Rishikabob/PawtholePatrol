package com.example.pawtholepatrol.feature.sim

import com.example.pawtholepatrol.feature.geo.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationSimulator(
    private val path: List<GeoPoint>,
    private val intervalMs: Long = 3000L
) {

    fun start(
        onLocationUpdate: (GeoPoint) -> Unit,
        onFinished: (() -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.Default).launch {

            for (point in path) {
                onLocationUpdate(point)
                delay(intervalMs)
            }

            delay(intervalMs)

            onFinished?.invoke()
        }
    }
}