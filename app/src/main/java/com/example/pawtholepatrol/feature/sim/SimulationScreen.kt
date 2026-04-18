package com.example.pawtholepatrol.feature.sim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.example.pawtholepatrol.feature.geo.GeoSpatialIndex
import com.example.pawtholepatrol.feature.monitor.HazardMonitor
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.example.pawtholepatrol.feature.validation.ValidationStore

@Composable
fun SimulationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var nearbyHazards by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationHelper.createChannels()
    }

    val pathToTraverse = listOf(
        GeoPoint(40.41, -79.91),
        GeoPoint(40.4, -79.9),
        GeoPoint(40.41, -79.91),
        GeoPoint(40.4004, -79.9004),
        GeoPoint(40.41, -79.91),
        GeoPoint(40.42, -79.92),
    )

    val hazardIndex = remember {
        GeoSpatialIndex().apply {
            addPoint(GeoPoint(40.4, -79.9))
            addPoint(GeoPoint(40.4008, -79.9008))
            addPoint(GeoPoint(40.42, -79.92))
        }
    }

    val monitor = remember {
        HazardMonitor(
            context = context,
            index = hazardIndex,
            radiusMeters = 100.0,
            notificationHelper = notificationHelper,
            onEvent = {},
            onHazardDetected = { point ->
                ValidationStore.addEvent(point)
            },
        )
    }

    val simulator = remember { LocationSimulator(pathToTraverse) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF2F7FF),
                        Color(0xFFE4EDFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Simulation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
            )

            Text(
                text = "Test hazard detection and location updates",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1C2F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Simulation Control",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (isRunning) return@Button
                            isRunning = true

                            simulator.start(
                                onLocationUpdate = { location ->
                                    currentLocation = location
                                    nearbyHazards = hazardIndex.findNearby(location, 100.0)
                                    monitor.onLocationUpdate(location)
                                },
                                onFinished = {
                                    isRunning = false
                                }
                            )
                        },
                        enabled = !isRunning,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (isRunning) "Running..." else "Start Simulation")
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Current Location",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    if (currentLocation == null || !isRunning) {
                        Text("None", color = Color(0xFF475569))
                    } else {
                        Text(
                            "Lat: %.5f\nLon: %.5f".format(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            ),
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Nearby Hazards",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    if (nearbyHazards.isEmpty() || !isRunning) {
                        Text("None", color = Color(0xFF475569))
                    } else {
                        nearbyHazards.forEachIndexed { index, hazard ->
                            Text(
                                text = "${index + 1}. Lat: %.5f, Lon: %.5f"
                                    .format(hazard.latitude, hazard.longitude),
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}
