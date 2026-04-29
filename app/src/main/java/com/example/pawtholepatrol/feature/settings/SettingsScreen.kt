package com.example.pawtholepatrol.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.pawtholepatrol.feature.notifications.NotificationHelper
import com.example.pawtholepatrol.utility.EventConfirmationHelper

private val ScreenGradientTop = Color(0xFFF2F7FF)
private val ScreenGradientBottom = Color(0xFFE4EDFF)
private val HeroCard = Color(0xFF0D1C2F)
private val HeroCardSoft = Color(0xFF243A5A)
private val SurfaceCard = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val DangerRed = Color(0xFFC1323E)
private val ActiveGreen = Color(0xFF15B97A)

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val runtimeState by TrackingRuntime.state.collectAsState()
    val notificationHelper = remember { NotificationHelper(context) }
    var alertDistance by rememberSaveable { mutableFloatStateOf(AppPreferences.getAlertDistanceMeters(context).toFloat()) }
    var soundEnabled by rememberSaveable { mutableStateOf(AppPreferences.isSoundEnabled(context)) }
    var inquiryEnabled by rememberSaveable { mutableStateOf(AppPreferences.isInquiryEnabled(context)) }
    var inquiryVisualEnabled by rememberSaveable { mutableStateOf(AppPreferences.isInquiryVisualEnabled(context))}
    var inquiryAudioEnabled by rememberSaveable { mutableStateOf(AppPreferences.isInquiryAudioEnabled(context)) }
    var autoDetectEnabled by rememberSaveable { mutableStateOf(AppPreferences.isAutoDetectEnabled(context)) }
    var showDeveloperSettings by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenGradientTop, ScreenGradientBottom))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Text(
                text = "Control detection behavior and developer tools",
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Permission Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    PermissionStatusRow("Location", hasAnyLocationPermission(context))
                    PermissionStatusRow("Activity Recognition", hasPermission(context, Manifest.permission.ACTIVITY_RECOGNITION))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionStatusRow("Notifications", hasPermission(context, Manifest.permission.POST_NOTIFICATIONS))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Tracking Modes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )

                    Text(
                        "Alert distance: ${alertDistance.toInt()}m",
                        color = TextSecondary,
                    )
                    Slider(
                        value = alertDistance,
                        onValueChange = {
                            alertDistance = it
                            AppPreferences.setAlertDistanceMeters(context, it.toInt())
                        },
                        valueRange = 40f..300f,
                    )
                    ToggleRow(
                        label = "Auto Detect",
                        description = "Use activity recognition to start/stop tracking in vehicle",
                        checked = autoDetectEnabled,
                        onCheckedChange = {
                            autoDetectEnabled = it
                            AppPreferences.setAutoDetectEnabled(context, it)
                            if (!it && runtimeState.isTracking && runtimeState.mode == TrackingMode.AUTO) {
                                context.stopService(Intent(context, PotholeDetectionService::class.java))
                            }
                        },
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Pothole Notification Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    ToggleRow(
                        label = "Text Speech Alerts",
                        description = "Text to Speech notification during pothole alerts",
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            AppPreferences.setSoundEnabled(context, it)
                        },
                    )
                    Button(
                        onClick = {
                            notificationHelper.showCriticalNotification(
                                "Hazard Ahead",
                                "You are entering a hazard area"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Test Pothole Notification", fontWeight = FontWeight.SemiBold)
                    }
                }
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Pothole Inquiry Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )

                    ToggleRow(
                        label = "Inquiry Enabled",
                        description = "Confirm pothole status during pothole event",
                        checked = inquiryEnabled,
                        onCheckedChange = { newValue ->
                            // Turn off visual and audio alerts if not enabled, and it's switched off
                            if (!newValue && inquiryVisualEnabled) {
                                inquiryVisualEnabled = false
                                AppPreferences.setInquiryVisualEnabled(context, false)
                            }
                            if (!newValue && inquiryAudioEnabled) {
                                inquiryAudioEnabled = false
                                AppPreferences.setInquiryAudioEnabled(context, false)
                            }

                            // Default to turn on just visual alert if switching the setting back on
                            if (newValue) {
                                inquiryVisualEnabled = true
                                AppPreferences.setInquiryVisualEnabled(context, true)
                            }

                            inquiryEnabled = newValue
                            AppPreferences.setInquiryEnabled(context, newValue)
                        },
                    )
                    ToggleRow(
                        label = "Visual Alerts",
                        description = "Can confirm potholes via notification",
                        checked = inquiryVisualEnabled,
                        onCheckedChange = { newValue ->
                            // If turning on but overall setting turned off, enable it
                            if (newValue && !AppPreferences.isInquiryEnabled(context)) {
                                inquiryEnabled = true
                                AppPreferences.setInquiryEnabled(context, true)
                            }

                            if (!newValue && !AppPreferences.isInquiryAudioEnabled(context)) {
                                // Turning off visual but audio is already off, force audio on
                                inquiryAudioEnabled = true
                                AppPreferences.setInquiryAudioEnabled(context, true)
                            }
                            inquiryVisualEnabled = newValue
                            AppPreferences.setInquiryVisualEnabled(context, newValue)
                        },
                    )
                    ToggleRow(
                        label = "Audio Alerts",
                        description = "Can confirm potholes via speech",
                        checked = inquiryAudioEnabled,
                        onCheckedChange = { newValue ->
                            // If turning on but overall setting turned off, enable it
                            if (newValue && !AppPreferences.isInquiryEnabled(context)) {
                                inquiryEnabled = true
                                AppPreferences.setInquiryEnabled(context, true)
                            }

                            if (!newValue && !inquiryVisualEnabled) {
                                // Turning off audio but visual is already off, force visual on
                                inquiryVisualEnabled = true
                                AppPreferences.setInquiryVisualEnabled(context, true)
                            }
                            inquiryAudioEnabled = newValue
                            AppPreferences.setInquiryAudioEnabled(context, newValue)
                        },
                    )
                    Button(
                        onClick = {
                            EventConfirmationHelper.askForConfirmation(
                                context,
                                "Was there a pothole?",
                                { confirmed ->
                                    if (confirmed) {
                                        Log.d("Visual Notification", "User confirmed the event")
                                    } else {
                                        Log.d("Visual Notification", "User denied or time out")
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Test Pothole Inquiry", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Help Demo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Button(
                            onClick = { showDeveloperSettings = !showDeveloperSettings },
                            colors = ButtonDefaults.buttonColors(containerColor = HeroCardSoft),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(if (showDeveloperSettings) "Hide" else "Open")
                        }
                    }

                    if (showDeveloperSettings) {
                        //TODO: remove simulation tab and add button to play simulation video
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(label: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFBFD2F2))
        Text(
            text = if (granted) "Granted" else "Missing",
            color = if (granted) ActiveGreen else Color(0xFFFF9FA8),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}

private fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun hasAnyLocationPermission(context: Context): Boolean {
    return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
        hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
}
