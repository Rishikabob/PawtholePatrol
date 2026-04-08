package com.example.pawtholepatrol.feature.validation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.DateFormat
import java.util.Date

private val ScreenGradientTop = Color(0xFFF2F7FF)
private val ScreenGradientBottom = Color(0xFFE4EDFF)
private val SurfaceCard = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val PendingBlue = Color(0xFF1E63DD)
private val ConfirmedGreen = Color(0xFF15B97A)
private val RejectedRed = Color(0xFFC1323E)

@Composable
fun ValidationScreen(modifier: Modifier = Modifier) {
    val events by ValidationStore.events.collectAsState()
    val pendingCount = events.count { it.status == ValidationStatus.PENDING }
    val confirmedCount = events.count { it.status == ValidationStatus.CONFIRMED }
    val rejectedCount = events.count { it.status == ValidationStatus.REJECTED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenGradientTop, ScreenGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Trip Validation",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Review simulator-detected pothole events",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        ValidationMap(events = events)

        SummaryCard(
            pendingCount = pendingCount,
            confirmedCount = confirmedCount,
            rejectedCount = rejectedCount,
        )

        if (events.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            ) {
                Text(
                    text = "No validation events yet. Run Simulation to generate events.",
                    modifier = Modifier.padding(16.dp),
                    color = TextSecondary,
                )
            }
        } else {
            events.forEach { event ->
                EventCard(event = event)
            }
        }
    }
}

@Composable
private fun ValidationMap(events: List<ValidationEvent>) {
    val context = LocalContext.current
    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(org.osmdroid.util.GeoPoint(40.41, -79.91))
            onResume()
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(events, mapView) {
        mapView.overlays.removeAll { it is Marker }

        events.forEach { event ->
            val marker = Marker(mapView).apply {
                position = org.osmdroid.util.GeoPoint(event.point.latitude, event.point.longitude)
                title = "Event #${event.id}"
                snippet = when (event.status) {
                    ValidationStatus.PENDING -> "Pending validation"
                    ValidationStatus.CONFIRMED -> "Confirmed"
                    ValidationStatus.REJECTED -> "Rejected"
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, markerIconForStatus(event.status))
            }
            mapView.overlays.add(marker)
        }

        if (events.isEmpty()) {
            mapView.controller.setZoom(13.0)
            mapView.controller.setCenter(org.osmdroid.util.GeoPoint(40.41, -79.91))
        } else if (events.size == 1) {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(
                org.osmdroid.util.GeoPoint(
                    events.first().point.latitude,
                    events.first().point.longitude,
                ),
            )
        } else {
            val points = events.map {
                org.osmdroid.util.GeoPoint(it.point.latitude, it.point.longitude)
            }
            val box = BoundingBox.fromGeoPoints(points)
            mapView.zoomToBoundingBox(box, false, 80)
        }

        mapView.invalidate()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pothole Map",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { mapView },
                update = {},
            )
        }
    }
}

private fun markerIconForStatus(status: ValidationStatus): Int {
    return when (status) {
        ValidationStatus.PENDING -> android.R.drawable.presence_away
        ValidationStatus.CONFIRMED -> android.R.drawable.presence_online
        ValidationStatus.REJECTED -> android.R.drawable.presence_busy
    }
}

@Composable
private fun SummaryCard(
    pendingCount: Int,
    confirmedCount: Int,
    rejectedCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountItem("Pending", pendingCount, PendingBlue)
            CountItem("Confirmed", confirmedCount, ConfirmedGreen)
            CountItem("Rejected", rejectedCount, RejectedRed)
        }
    }
}

@Composable
private fun CountItem(
    label: String,
    value: Int,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun EventCard(event: ValidationEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Event #${event.id}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "Lat ${"%.5f".format(event.point.latitude)}, Lon ${"%.5f".format(event.point.longitude)}",
                color = TextSecondary,
            )
            Text(
                text = DateFormat.getDateTimeInstance().format(Date(event.createdAtMillis)),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )

            if (event.status == ValidationStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { ValidationStore.confirm(event.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ConfirmedGreen),
                    ) {
                        Text("Correct")
                    }
                    Button(
                        onClick = { ValidationStore.reject(event.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RejectedRed),
                    ) {
                        Text("Incorrect")
                    }
                }
            } else {
                val statusColor = if (event.status == ValidationStatus.CONFIRMED) ConfirmedGreen else RejectedRed
                Text(
                    text = if (event.status == ValidationStatus.CONFIRMED) "Status: Confirmed" else "Status: Rejected",
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
