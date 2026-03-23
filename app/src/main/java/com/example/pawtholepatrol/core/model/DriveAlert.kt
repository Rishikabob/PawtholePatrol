package com.example.pawtholepatrol.core.model

data class DriveAlert(
    val message: String,
    val potholeId: String,
    val distanceMeters: Int,
    val visual: Boolean = true,
    val audio: Boolean = true,
)
