package com.example.pawtholepatrol.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.core.model.DriveSession

@Composable
fun CaptureScreen(
    activeSession: DriveSession?,
    onStartStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Crowd Data Capture", style = MaterialTheme.typography.titleMedium)
                Text("Record accelerometer + GPS while the driver is moving.")
                Text(
                    if (activeSession == null) {
                        "No active session"
                    } else {
                        "Active session: ${activeSession.id.take(8)}"
                    }
                )
                Button(onClick = onStartStop) {
                    Text(if (activeSession == null) "Start Session" else "Stop Session")
                }
            }
        }

        Text(
            "Scaffold note: connect this screen to a foreground service for resilient background collection.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
