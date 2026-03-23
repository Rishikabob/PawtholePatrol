package com.example.pawtholepatrol.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val defaultCenter = LatLng(40.7580, -73.9855)
    val cameraPositionState = rememberCameraPositionState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Map", style = MaterialTheme.typography.titleMedium)
                Text("Live Google Map")
                Text("Data markers will be added next")
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(top = 12.dp),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        mapToolbarEnabled = false,
                    ),
                ) {
                    Marker(
                        state = MarkerState(position = defaultCenter),
                        title = "Default Center",
                    )
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(defaultCenter, 13f)
        )
    }
}
