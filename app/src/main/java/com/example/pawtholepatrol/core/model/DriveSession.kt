package com.example.pawtholepatrol.core.model

enum class UploadStatus {
    PENDING,
    UPLOADED,
    FAILED,
}

data class DriveSession(
    val id: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null,
    val samplesCount: Int = 0,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
)
