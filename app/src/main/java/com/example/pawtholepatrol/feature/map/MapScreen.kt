package com.example.pawtholepatrol.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.core.model.Pothole

@Composable
fun MapScreen(
    potholes: List<Pothole>,
    onRefresh: () -> Unit,
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
                    Text("Map Layer", style = MaterialTheme.typography.titleMedium)
                    Text("Placeholder map: integrate Google Maps SDK here.")
                    Text("Nearby potholes currently loaded: ${potholes.size}")
                    Button(onClick = onRefresh) {
                        Text("Refresh Nearby")
                    }
                }
            }
        }

        items(potholes, key = { it.id }) { pothole ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${pothole.id} • ${pothole.severity}")
                    Text("Lat ${"%.5f".format(pothole.location.latitude)}, Lng ${"%.5f".format(pothole.location.longitude)}")
                    Text("Confidence ${(pothole.confidence * 100).toInt()}% • ${pothole.source}")
                }
            }
        }
    }
}
