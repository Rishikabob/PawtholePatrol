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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pawtholepatrol.feature.capture.CaptureScreen
import com.example.pawtholepatrol.feature.map.MapScreen
import com.example.pawtholepatrol.feature.route.RouteScreen
import com.example.pawtholepatrol.feature.settings.SettingsScreen

private enum class AppTab(val label: String, val iconText: String) {
    MAP("Map", "M"),
    CAPTURE("Capture", "C"),
    ROUTE("Route", "R"),
    SETTINGS("Settings", "S"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawtholeApp() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.MAP.name) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Pawthole Patrol") })
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.name,
                        onClick = { selectedTab = tab.name },
                        icon = { Text(tab.iconText) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (AppTab.valueOf(selectedTab)) {
            AppTab.MAP -> MapScreen(modifier = Modifier.padding(innerPadding))
            AppTab.CAPTURE -> CaptureScreen(modifier = Modifier.padding(innerPadding))
            AppTab.ROUTE -> RouteScreen(modifier = Modifier.padding(innerPadding))
            AppTab.SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
