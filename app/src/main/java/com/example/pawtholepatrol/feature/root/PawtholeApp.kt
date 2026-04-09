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
import com.example.pawtholepatrol.feature.home.HomeScreen
import com.example.pawtholepatrol.feature.manual.ManualEntryScreen
import com.example.pawtholepatrol.feature.sim.SimulationScreen
import com.example.pawtholepatrol.feature.settings.SettingsScreen
import com.example.pawtholepatrol.feature.validation.ValidationScreen

private enum class AppTab(val label: String, val iconText: String) {
    HOME("Home", "H"),
    MANUAL("Manual", "M"),
    VALIDATION(label = "Validation", iconText = "V"),
    SETTINGS("Settings", "S"),
    SIMULATION(label = "Simulation", iconText = "SIM"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawtholeApp() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME.name) }

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
            AppTab.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
            AppTab.MANUAL -> ManualEntryScreen(modifier = Modifier.padding(innerPadding))
            AppTab.VALIDATION -> ValidationScreen(modifier = Modifier.padding(innerPadding))
            AppTab.SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            AppTab.SIMULATION -> SimulationScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
