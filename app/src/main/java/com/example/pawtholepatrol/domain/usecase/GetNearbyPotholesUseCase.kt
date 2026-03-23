package com.example.pawtholepatrol.domain.usecase

import com.example.pawtholepatrol.core.model.GeoPoint
import com.example.pawtholepatrol.core.model.Pothole
import com.example.pawtholepatrol.domain.repository.PotholeRepository

class GetNearbyPotholesUseCase(
    private val potholeRepository: PotholeRepository,
) {
    suspend operator fun invoke(center: GeoPoint, radiusMeters: Int = 2_000): List<Pothole> {
        return potholeRepository.getNearbyPotholes(center, radiusMeters)
    }
}
