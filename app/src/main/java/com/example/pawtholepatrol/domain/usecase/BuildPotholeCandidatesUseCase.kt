package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.core.model.Pothole
import com.example.pawtholepatrol.core.model.SensorSample
import com.example.pawtholepatrol.domain.engine.PotholeDetectionEngine

class BuildPotholeCandidatesUseCase(
    private val detectionEngine: PotholeDetectionEngine,
) {
    operator fun invoke(samples: List<SensorSample>, sensitivity: Float): List<Pothole> {
        return detectionEngine.detectCandidates(samples, sensitivity)
    }
}
