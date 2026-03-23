package com.example.pawtholepatrol.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawtholepatrol.core.model.UserPrefs
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    prefs: UserPrefs,
    onPrefsChanged: (UserPrefs) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Alert Settings", style = MaterialTheme.typography.titleMedium)
                Text("Alert distance: ${prefs.alertDistanceMeters}m")
                Slider(
                    value = prefs.alertDistanceMeters.toFloat(),
                    onValueChange = { value ->
                        onPrefsChanged(prefs.copy(alertDistanceMeters = value.roundToInt()))
                    },
                    valueRange = 40f..300f,
                )

                ToggleRow(
                    label = "Sound alerts",
                    checked = prefs.soundEnabled,
                    onCheckedChange = { onPrefsChanged(prefs.copy(soundEnabled = it)) },
                )

                ToggleRow(
                    label = "Voice (TTS) alerts",
                    checked = prefs.ttsEnabled,
                    onCheckedChange = { onPrefsChanged(prefs.copy(ttsEnabled = it)) },
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
