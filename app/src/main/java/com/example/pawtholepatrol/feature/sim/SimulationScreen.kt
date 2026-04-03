package com.example.pawtholepatrol.feature.sim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.monitor.HazardMonitor
import com.example.pawtholepatrol.feature.notifications.AlertBanner
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import kotlinx.coroutines.*

@Composable
fun SimulationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var nearbyHazards by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    var bannerMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        notificationHelper.createChannels()
    }

//    val hazards = listOf(
//        GeoPoint(40.4533953856059, -79.9469436680444),
//        GeoPoint(40.4531974456379, -79.9475791078329),
//        GeoPoint(40.4520497943914, -79.9494989987492),
//        GeoPoint(40.4515073478504, -79.9492576228172),
//        GeoPoint(40.4512426030564, -79.9491365755203),
//        GeoPoint(40.4510547976252, -79.9490503726385),
//        GeoPoint(40.4508547148008, -79.9489585344209),
//        GeoPoint(40.4453094372525, -79.9483909280942),
//        GeoPoint(40.4457845730441, -79.9494342823836),
//        GeoPoint(40.445763126924, -79.9496695432699),
//        GeoPoint(40.446286682318, -79.9500484353365),
//        GeoPoint(40.4462987774823, -79.949898771809),
//        GeoPoint(40.4463266438935, -79.949580295764)
//    )

    val pathToTraverse = listOf(
        GeoPoint(40.4, -79.9),
        GeoPoint(40.41, -79.91),
        GeoPoint(40.4, -79.9),
        GeoPoint(40.41, -79.91),
        GeoPoint(40.42, -79.92),
    )


    val hazardIndex = GeoSpatialIndex()
    //index.addPoints(hazards)
    hazardIndex.addPoint(GeoPoint(40.4 ,-79.9))
    hazardIndex.addPoint(GeoPoint(40.42 ,-79.92))


    val delayBetweenSteps = 3000.toLong()

    val monitor = HazardMonitor(
        index = hazardIndex,
        radiusMeters = 100.0,
        notificationHelper = notificationHelper
    ) { message ->

        bannerMessage = message

        CoroutineScope(Dispatchers.Main).launch {
            delay(delayBetweenSteps)
            bannerMessage = null
        }
    }

    val simulator = LocationSimulator(pathToTraverse)

    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            AlertBanner(
                message = bannerMessage ?: "",
                backgroundColor = Color.Red
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = {
                bannerMessage = "Hazard ahead!"

                CoroutineScope(Dispatchers.Main).launch {
                    delay(delayBetweenSteps)
                    bannerMessage = null
                }

                notificationHelper.showCriticalNotification(
                    "Hazard Alert",
                    "Test hazard detected ahead"
                )
            }) {
                Text("Test In-app Critical Notification")
            }

            Button(onClick = {
                bannerMessage = "Pothole Detected"

                CoroutineScope(Dispatchers.Main).launch {
                    delay(delayBetweenSteps)
                    bannerMessage = null
                }

                notificationHelper.showGeneralNotification(
                    "Pothole Detected",
                    "A new pothole location has been detected"
                )
            }) {
                Text("Test In-app General Notification")
            }

            var isRunning by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    if (isRunning) return@Button

                    isRunning = true

                    simulator.start(
                        onLocationUpdate = { location ->
                            currentLocation = location

                            val nearby = hazardIndex.findNearby(location, 100.0)
                            nearbyHazards = nearby

                            monitor.onLocationUpdate(location)
                        },
                        onFinished = {
                            isRunning = false

                            bannerMessage = "Simulation complete"

                            CoroutineScope(Dispatchers.Main).launch {
                                delay(delayBetweenSteps)
                                bannerMessage = null
                            }
                        }
                    )
                },
                enabled = !isRunning
            ) {
                Text(if (isRunning) "Running..." else "Start Simulation")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Nearby Hazards:")

            if (nearbyHazards.isEmpty() || !isRunning) {
                Text("None")
            } else {
                nearbyHazards.forEachIndexed { index, hazard ->
                    Text(
                        text = "${index + 1}. Lat: %.5f, Lon: %.5f"
                            .format(hazard.latitude, hazard.longitude)
                    )
                }
            }
        }
    }
}