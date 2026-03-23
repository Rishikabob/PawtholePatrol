package com.example.pawtholepatrol.data

import com.example.pawtholepatrol.data.repo.FakePotholeRepository
import com.example.pawtholepatrol.data.repo.FakePreferencesRepository
import com.example.pawtholepatrol.data.repo.FakeRoutingRepository
import com.example.pawtholepatrol.data.repo.FakeSensorRepository
import com.example.pawtholepatrol.domain.engine.AlertEngine
import com.example.pawtholepatrol.domain.engine.PotholeDetectionEngine
import com.example.pawtholepatrol.domain.repository.PotholeRepository
import com.example.pawtholepatrol.domain.repository.PreferencesRepository
import com.example.pawtholepatrol.domain.repository.RoutingRepository
import com.example.pawtholepatrol.domain.repository.SensorRepository
import com.example.pawtholepatrol.domain.usecase.BuildDriveAlertsUseCase
import com.example.pawtholepatrol.domain.usecase.BuildPotholeCandidatesUseCase
import com.example.pawtholepatrol.domain.usecase.GetNearbyPotholesUseCase
import com.example.pawtholepatrol.domain.usecase.GetRouteHazardsUseCase
import com.example.pawtholepatrol.domain.usecase.StartDriveSessionUseCase
import com.example.pawtholepatrol.domain.usecase.StopDriveSessionUseCase

interface AppContainer {
    val potholeRepository: PotholeRepository
    val sensorRepository: SensorRepository
    val routingRepository: RoutingRepository
    val preferencesRepository: PreferencesRepository

    val getNearbyPotholesUseCase: GetNearbyPotholesUseCase
    val startDriveSessionUseCase: StartDriveSessionUseCase
    val stopDriveSessionUseCase: StopDriveSessionUseCase
    val getRouteHazardsUseCase: GetRouteHazardsUseCase
    val buildDriveAlertsUseCase: BuildDriveAlertsUseCase
    val buildPotholeCandidatesUseCase: BuildPotholeCandidatesUseCase
}

class DefaultAppContainer : AppContainer {
    override val potholeRepository: PotholeRepository = FakePotholeRepository()
    override val sensorRepository: SensorRepository = FakeSensorRepository()
    override val routingRepository: RoutingRepository = FakeRoutingRepository()
    override val preferencesRepository: PreferencesRepository = FakePreferencesRepository()

    private val alertEngine = AlertEngine()
    private val detectionEngine = PotholeDetectionEngine()

    override val getNearbyPotholesUseCase = GetNearbyPotholesUseCase(potholeRepository)
    override val startDriveSessionUseCase = StartDriveSessionUseCase(sensorRepository)
    override val stopDriveSessionUseCase = StopDriveSessionUseCase(sensorRepository)
    override val getRouteHazardsUseCase = GetRouteHazardsUseCase(routingRepository, potholeRepository)
    override val buildDriveAlertsUseCase = BuildDriveAlertsUseCase(alertEngine, preferencesRepository)
    override val buildPotholeCandidatesUseCase = BuildPotholeCandidatesUseCase(detectionEngine)
}
