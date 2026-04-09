package com.example.pawtholepatrol.feature.manual

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.pawtholepatrol.PotholeCsvStore
import com.example.pawtholepatrol.feature.geo.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.FolderOverlay
import kotlin.coroutines.resume

private val ScreenGradientTop = Color(0xFFF2F7FF)
private val ScreenGradientBottom = Color(0xFFE4EDFF)
private val SurfaceCard = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val SaveBlue = Color(0xFF1E63DD)
private val DangerRed = Color(0xFFC1323E)
private val LocationBlue = Color(0xFF2563EB)
private val DefaultCenter = GeoPoint(40.41, -79.91)

data class ManualEntryUiState(
    val currentCenter: GeoPoint? = null,
    val pendingPoint: GeoPoint? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
)

@Composable
fun ManualEntryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val points = remember { mutableStateListOf<GeoPoint>() }
    var uiState by remember { mutableStateOf(ManualEntryUiState()) }
    var hasCenteredByLocation by remember { mutableStateOf(false) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var jumpToPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val onLongPressState = rememberUpdatedState<(GeoPoint) -> Unit> { point ->
        uiState = uiState.copy(pendingPoint = point, error = null)
    }

    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.5)
            controller.setCenter(org.osmdroid.util.GeoPoint(DefaultCenter.latitude, DefaultCenter.longitude))

            val mapEventsOverlay = MapEventsOverlay(
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: org.osmdroid.util.GeoPoint?): Boolean = false

                    override fun longPressHelper(p: org.osmdroid.util.GeoPoint?): Boolean {
                        if (p == null) return false
                        onLongPressState.value(
                            GeoPoint(
                                latitude = p.latitude,
                                longitude = p.longitude,
                            ),
                        )
                        return true
                    }
                },
            )
            overlays.add(mapEventsOverlay)
            overlays.add(FolderOverlay().apply { name = "pothole_markers" })
            onResume()
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(Unit) {
        val saved = PotholeCsvStore.readAll(context)
        points.clear()
        points.addAll(saved)

        val currentLocation = fetchCurrentLocation(context)
        uiState = uiState.copy(currentCenter = currentLocation)
    }

    LaunchedEffect(uiState.currentCenter, mapView, hasCenteredByLocation) {
        if (!hasCenteredByLocation && uiState.currentCenter != null) {
            val center = uiState.currentCenter ?: return@LaunchedEffect
            mapView.controller.setZoom(16.0)
            mapView.controller.setCenter(org.osmdroid.util.GeoPoint(center.latitude, center.longitude))
            hasCenteredByLocation = true
        }
    }

    LaunchedEffect(jumpToPoint, mapView) {
        val target = jumpToPoint ?: return@LaunchedEffect
        mapView.controller.setZoom(17.0)
        mapView.controller.animateTo(org.osmdroid.util.GeoPoint(target.latitude, target.longitude))
        jumpToPoint = null
    }

    LaunchedEffect(points.size, uiState.pendingPoint, uiState.currentCenter, mapView) {
        val markerOverlay = mapView.overlays.firstOrNull { it is FolderOverlay && it.name == "pothole_markers" } as? FolderOverlay
            ?: FolderOverlay().apply { name = "pothole_markers" }.also { mapView.overlays.add(it) }
        markerOverlay.items.clear()

        uiState.currentCenter?.let { point ->
            val marker = Marker(mapView).apply {
                position = org.osmdroid.util.GeoPoint(point.latitude, point.longitude)
                title = "Your location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
            }
            markerOverlay.add(marker)
        }

        points.forEachIndexed { index, point ->
            val marker = Marker(mapView).apply {
                position = org.osmdroid.util.GeoPoint(point.latitude, point.longitude)
                title = "Pothole #${index + 1}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = numberedPotholeIcon(context, index + 1)
            }
            markerOverlay.add(marker)
        }

        uiState.pendingPoint?.let { point ->
            val marker = Marker(mapView).apply {
                position = org.osmdroid.util.GeoPoint(point.latitude, point.longitude)
                title = "Pending save"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, android.R.drawable.presence_away)
            }
            markerOverlay.add(marker)
        }

        mapView.invalidate()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenGradientTop, ScreenGradientBottom)))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Manual Entry",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Long-press on map to add potholes near your current location.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    factory = { mapView },
                    update = {},
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Saved potholes: ${points.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )

                if (uiState.pendingPoint == null) {
                    Text(
                        text = "No pending point. Long-press map to add one.",
                        color = TextSecondary,
                    )
                } else {
                    Text(
                        text = "Pending: Lat ${"%.5f".format(uiState.pendingPoint?.latitude)}, Lon ${"%.5f".format(uiState.pendingPoint?.longitude)}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                val pending = uiState.pendingPoint ?: return@Button
                                coroutineScope.launch {
                                    try {
                                        uiState = uiState.copy(isSaving = true, error = null)
                                        withContext(Dispatchers.IO) {
                                            PotholeCsvStore.append(context, pending)
                                        }
                                        points.add(pending)
                                        uiState = uiState.copy(isSaving = false, pendingPoint = null)
                                    } catch (exception: Exception) {
                                        uiState = uiState.copy(
                                            isSaving = false,
                                            error = exception.message ?: "Unable to save pothole",
                                        )
                                    }
                                }
                            },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SaveBlue),
                        ) {
                            Text(if (uiState.isSaving) "Saving..." else "Save pothole")
                        }
                        Button(
                            onClick = {
                                uiState = uiState.copy(pendingPoint = null, error = null)
                            },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        ) {
                            Text("Cancel")
                        }
                    }
                }

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = DangerRed,
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val center = uiState.currentCenter ?: fetchCurrentLocation(context)
                            if (center != null) {
                                uiState = uiState.copy(currentCenter = center, error = null)
                                jumpToPoint = center
                            } else {
                                uiState = uiState.copy(
                                    error = "Current location unavailable",
                                )
                            }
                        }
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LocationBlue),
                ) {
                    Text("Go to My Location")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Saved Potholes",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )

                if (points.isEmpty()) {
                    Text(
                        text = "No saved potholes yet.",
                        color = TextSecondary,
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        points.forEachIndexed { index, point ->
                            val isSelected = selectedPointIndex == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFFE7F0FF) else Color.Transparent,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable {
                                        selectedPointIndex = index
                                        jumpToPoint = point
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = "Pothole #${index + 1}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Text(
                                        text = "Lat ${"%.5f".format(point.latitude)}, Lon ${"%.5f".format(point.longitude)}",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                TextButton(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(32.dp),
                                    enabled = !uiState.isSaving,
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                if (index !in points.indices) return@launch
                                                uiState = uiState.copy(isSaving = true, error = null)
                                                val updated = points.toMutableList().apply { removeAt(index) }
                                                withContext(Dispatchers.IO) {
                                                    PotholeCsvStore.replaceAll(context, updated)
                                                }
                                                points.clear()
                                                points.addAll(updated)
                                                selectedPointIndex = when (val selected = selectedPointIndex) {
                                                    null -> null
                                                    index -> null
                                                    in 0 until index -> selected
                                                    else -> (selected - 1).takeIf { it in updated.indices }
                                                }
                                                uiState = uiState.copy(isSaving = false)
                                            } catch (exception: Exception) {
                                                uiState = uiState.copy(
                                                    isSaving = false,
                                                    error = exception.message ?: "Unable to delete pothole",
                                                )
                                            }
                                        }
                                    },
                                ) {
                                    Text("Delete", color = DangerRed)
                                }
                            }
                            if (index < points.lastIndex) {
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun numberedPotholeIcon(context: Context, number: Int): BitmapDrawable {
    val sizePx = 96
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1E63DD.toInt()
        style = Paint.Style.FILL
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    val cx = sizePx / 2f
    val cy = sizePx / 2f
    canvas.drawCircle(cx, cy, sizePx * 0.36f, circlePaint)
    val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(number.toString(), cx, baseline, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private suspend fun fetchCurrentLocation(context: Context): GeoPoint? {
    val hasFine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasFine && !hasCoarse) return null

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    val current = suspendCancellableCoroutine<GeoPoint?> { continuation ->
        val tokenSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                val point = if (location != null) {
                    GeoPoint(location.latitude, location.longitude)
                } else {
                    null
                }
                if (continuation.isActive) continuation.resume(point)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }

        continuation.invokeOnCancellation {
            tokenSource.cancel()
        }
    }

    if (current != null) return current

    val last = awaitTask(fusedClient.lastLocation)
    return if (last != null) GeoPoint(last.latitude, last.longitude) else null
}

private suspend fun <T> awaitTask(task: Task<T>): T? {
    return suspendCancellableCoroutine { continuation ->
        task.addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
