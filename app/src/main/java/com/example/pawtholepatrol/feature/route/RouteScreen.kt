package com.example.pawtholepatrol.feature.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.core.model.DriveAlert
import com.example.pawtholepatrol.core.model.RouteHazard
import com.example.pawtholepatrol.core.model.RoutePath

@Composable
fun RouteScreen(
    destinationQuery: String,
    onDestinationChange: (String) -> Unit,
    route: RoutePath?,
    hazards: List<RouteHazard>,
    alerts: List<DriveAlert>,
    onBuildRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Route Planner", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = destinationQuery,
                        onValueChange = onDestinationChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter destination") },
                        singleLine = true,
                    )
                    Button(onClick = onBuildRoute, enabled = destinationQuery.isNotBlank()) {
                        Text("Build Route + Hazards")
                    }
                    Text(
                        if (route == null) {
                            "No route loaded"
                        } else {
                            "Route loaded: ${route.distanceMeters}m • ${route.durationSeconds}s"
                        }
                    )
                    Text("Hazards on route: ${hazards.size}")
                    Text("Immediate driving alerts: ${alerts.size}")
                }
            }
        }

        if (hazards.isNotEmpty()) {
            item {
                Text("Hazards", style = MaterialTheme.typography.titleSmall)
            }
            items(hazards, key = { it.pothole.id }) { hazard ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${hazard.pothole.severity} • ${hazard.distanceAlongRouteMeters}m ahead")
                        Text("ETA ${hazard.etaSeconds}s • ID ${hazard.pothole.id}")
                    }
                }
            }
        }

        if (alerts.isNotEmpty()) {
            item {
                Text("Live Alerts", style = MaterialTheme.typography.titleSmall)
            }
            items(alerts, key = { it.potholeId + it.distanceMeters }) { alert ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(alert.message)
                        Text("Audio ${if (alert.audio) "ON" else "OFF"} • Visual ${if (alert.visual) "ON" else "OFF"}")
                    }
                }
            }
        }
    }
}
