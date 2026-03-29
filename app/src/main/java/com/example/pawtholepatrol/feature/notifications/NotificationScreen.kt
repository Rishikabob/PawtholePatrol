package com.example.pawtholepatrol.feature.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var bannerMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        notificationHelper.createChannel()
    }

    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            AlertBanner(
                message = bannerMessage ?: "",
                backgroundColor = Color.Red
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = {
                bannerMessage = "Hazard ahead!"

                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000)
                    bannerMessage = null
                }

                notificationHelper.showNotification(
                    "Hazard Alert",
                    "Test hazard detected ahead"
                )
            }) {
                Text("Test In-app Notification")
            }
        }
    }
}