package com.example.pawtholepatrol.core.model

data class UserPrefs(
    val alertDistanceMeters: Int = 120,
    val soundEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val detectionSensitivity: Float = 0.7f,
)
