package com.example.pawtholepatrol

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TrackingMode {
    MANUAL,
    AUTO,
}

data class TrackingRuntimeState(
    val isTracking: Boolean = false,
    val mode: TrackingMode? = null,
)

object TrackingRuntime {
    private val mutableState = MutableStateFlow(TrackingRuntimeState())
    val state: StateFlow<TrackingRuntimeState> = mutableState.asStateFlow()

    fun onStarted(mode: TrackingMode) {
        mutableState.value = TrackingRuntimeState(
            isTracking = true,
            mode = mode,
        )
    }

    fun onStopped() {
        mutableState.value = TrackingRuntimeState(
            isTracking = false,
            mode = null,
        )
    }
}
