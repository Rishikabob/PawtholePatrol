package com.example.pawtholepatrol.feature.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pawtholepatrol.AppPreferences
import com.example.pawtholepatrol.PotholeDetectionService
import com.example.pawtholepatrol.TrackingMode
import com.example.pawtholepatrol.TrackingRuntime

private val ScreenGradientTop = Color(0xFFF2F7FF)
private val ScreenGradientBottom = Color(0xFFE4EDFF)
private val HeroCard = Color(0xFF0D1C2F)
private val HeroCardSoft = Color(0xFF243A5A)
private val SurfaceCard = Color(0xFFFFFFFF)
private val SurfaceCardMuted = Color(0xFFF5F8FF)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val ActiveGreen = Color(0xFF15B97A)
private val IdleGray = Color(0xFF64748B)
private val AccentBlue = Color(0xFF1E63DD)
private val DangerRed = Color(0xFFC1323E)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val runtimeState by TrackingRuntime.state.collectAsState()
    val autoDetectEnabled = AppPreferences.isAutoDetectEnabled(context)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        if (hasLocationPermission(context)) {
            startManualTracking(context)
        } else {
            Toast.makeText(context, "Location permission is required to start tracking", Toast.LENGTH_SHORT).show()
        }
    }

    val isTracking = runtimeState.isTracking
    val modeLabel = when (runtimeState.mode) {
        TrackingMode.MANUAL -> "Manual"
        TrackingMode.AUTO -> "Auto Detect"
        null -> "Idle"
    }
    val hasLocation = hasLocationPermission(context)
    val statusColor by animateColorAsState(
        targetValue = if (isTracking) ActiveGreen else IdleGray,
        label = "statusColor",
    )

    val pulseTransition = rememberInfiniteTransition(label = "trackingPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenGradientTop, ScreenGradientBottom))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Pawthole Patrol",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Text(
                text = "Live vehicle tracking dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HeroCard),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = if (isTracking) pulseAlpha else 1f)),
                            )
                            Text(
                                text = if (isTracking) "Tracking Active" else "Tracking Idle",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(HeroCardSoft)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFD9E6FF),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Text(
                        text = if (isTracking) {
                            "Recording GPS + accelerometer in the foreground service"
                        } else {
                            "Start manual tracking or wait for Auto Detect to trigger"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB9C9E6),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DashboardPill(
                            label = "Location",
                            value = if (hasLocation) "Granted" else "Missing",
                            tint = if (hasLocation) ActiveGreen else DangerRed,
                            modifier = Modifier.weight(1f),
                        )
                        DashboardPill(
                            label = "Auto Detect",
                            value = if (autoDetectEnabled) "Enabled" else "Disabled",
                            tint = if (autoDetectEnabled) AccentBlue else IdleGray,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ModeTile(
                    title = "Manual",
                    subtitle = "Tap to control",
                    isActive = runtimeState.mode == TrackingMode.MANUAL && isTracking,
                    activeColor = ActiveGreen,
                    modifier = Modifier.weight(1f),
                )
                ModeTile(
                    title = "Auto",
                    subtitle = "Vehicle detection",
                    isActive = runtimeState.mode == TrackingMode.AUTO && isTracking,
                    activeColor = AccentBlue,
                    modifier = Modifier.weight(1f),
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Session Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )

                    Button(
                        onClick = {
                            if (isTracking) {
                                val stopIntent = Intent(context, PotholeDetectionService::class.java)
                                context.stopService(stopIntent)
                            } else if (hasLocation) {
                                startManualTracking(context)
                            } else {
                                val permissions = buildList {
                                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                permissionLauncher.launch(permissions.toTypedArray())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) DangerRed else AccentBlue,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = if (isTracking) "Stop Tracking" else "Start Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Text(
                        text = if (hasLocation) {
                            "Permissions ready for location tracking"
                        } else {
                            "Grant location permission to start tracking"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardPill(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HeroCardSoft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFAFC1E0),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
    }
}

@Composable
private fun ModeTile(
    title: String,
    subtitle: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) SurfaceCard else SurfaceCardMuted,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Text(
                text = if (isActive) "Active" else "Standby",
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) activeColor else IdleGray,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun startManualTracking(context: Context) {
    val startIntent = Intent(context, PotholeDetectionService::class.java).apply {
        action = PotholeDetectionService.ACTION_START
        putExtra(PotholeDetectionService.EXTRA_MODE, TrackingMode.MANUAL.name)
    }
    ContextCompat.startForegroundService(context, startIntent)
}
