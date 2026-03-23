package com.example.pawtholepatrol.feature.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pawtholepatrol.core.model.DriveAlert
import com.example.pawtholepatrol.core.model.DriveSession
import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.Pothole
import com.example.pawtholepatrol.core.model.RouteHazard
import com.example.pawtholepatrol.core.model.RoutePath
import com.example.pawtholepatrol.core.model.UserPrefs
import com.example.pawtholepatrol.data.AppContainer
import com.example.pawtholepatrol.data.DefaultAppContainer
import com.example.pawtholepatrol.feature.capture.CaptureScreen
import com.example.pawtholepatrol.feature.map.MapScreen
import com.example.pawtholepatrol.feature.route.RouteScreen
import com.example.pawtholepatrol.feature.settings.SettingsScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private enum class AppTab(val label: String, val iconText: String) {
    MAP("Map", "M"),
    CAPTURE("Capture", "C"),
    ROUTE("Route", "R"),
    SETTINGS("Settings", "S"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawtholeApp(
    container: AppContainer = remember { DefaultAppContainer() },
) {
    val scope = rememberCoroutineScope()
    val currentLocation = remember { GeoPoint(latitude = 40.7580, longitude = -73.9855) }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.MAP.name) }
    var nearbyPotholes by remember { mutableStateOf<List<Pothole>>(emptyList()) }
    var activeSession by remember { mutableStateOf<DriveSession?>(null) }
    var destinationQuery by rememberSaveable { mutableStateOf("") }
    var routePath by remember { mutableStateOf<RoutePath?>(null) }
    var routeHazards by remember { mutableStateOf<List<RouteHazard>>(emptyList()) }
    var driveAlerts by remember { mutableStateOf<List<DriveAlert>>(emptyList()) }
    var prefs by remember { mutableStateOf(UserPrefs()) }

    LaunchedEffect(Unit) {
        nearbyPotholes = container.getNearbyPotholesUseCase(currentLocation)
    }

    LaunchedEffect(Unit) {
        container.preferencesRepository.observeUserPrefs().collectLatest { latestPrefs ->
            prefs = latestPrefs
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pawthole Patrol") },
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.name,
                        onClick = { selectedTab = tab.name },
                        icon = {
                            Text(tab.iconText)
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (AppTab.valueOf(selectedTab)) {
            AppTab.MAP -> MapScreen(
                potholes = nearbyPotholes,
                onRefresh = {
                    scope.launch {
                        nearbyPotholes = container.getNearbyPotholesUseCase(currentLocation)
                    }
                },
                modifier = Modifier.padding(innerPadding),
            )

            AppTab.CAPTURE -> CaptureScreen(
                activeSession = activeSession,
                onStartStop = {
                    scope.launch {
                        if (activeSession == null) {
                            activeSession = container.startDriveSessionUseCase()
                        } else {
                            container.stopDriveSessionUseCase(activeSession!!.id)
                            activeSession = null
                        }
                    }
                },
                modifier = Modifier.padding(innerPadding),
            )

            AppTab.ROUTE -> RouteScreen(
                destinationQuery = destinationQuery,
                onDestinationChange = { destinationQuery = it },
                route = routePath,
                hazards = routeHazards,
                alerts = driveAlerts,
                onBuildRoute = {
                    scope.launch {
                        val result = container.getRouteHazardsUseCase(
                            origin = currentLocation,
                            destinationQuery = destinationQuery,
                        )
                        routePath = result.route
                        routeHazards = result.hazards
                        driveAlerts = container.buildDriveAlertsUseCase(currentLocation, result.hazards)
                    }
                },
                modifier = Modifier.padding(innerPadding),
            )

            AppTab.SETTINGS -> SettingsScreen(
                prefs = prefs,
                onPrefsChanged = { updatedPrefs ->
                    scope.launch {
                        container.preferencesRepository.updateUserPrefs(updatedPrefs)
                    }
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
