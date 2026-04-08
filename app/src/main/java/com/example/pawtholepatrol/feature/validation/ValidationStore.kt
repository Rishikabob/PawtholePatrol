package com.example.pawtholepatrol.feature.validation

import com.example.pawtholepatrol.feature.geo.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

enum class ValidationStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
}

data class ValidationEvent(
    val id: Long,
    val point: GeoPoint,
    val createdAtMillis: Long,
    val status: ValidationStatus = ValidationStatus.PENDING,
)

object ValidationStore {
    private val idGenerator = AtomicLong(1L)
    private val mutableEvents = MutableStateFlow<List<ValidationEvent>>(emptyList())
    val events: StateFlow<List<ValidationEvent>> = mutableEvents.asStateFlow()

    fun addEvent(point: GeoPoint) {
        val event = ValidationEvent(
            id = idGenerator.getAndIncrement(),
            point = point,
            createdAtMillis = System.currentTimeMillis(),
        )
        mutableEvents.update { current -> listOf(event) + current }
    }

    fun confirm(eventId: Long) {
        mutableEvents.update { current ->
            current.map { event ->
                if (event.id == eventId) event.copy(status = ValidationStatus.CONFIRMED) else event
            }
        }
    }

    fun reject(eventId: Long) {
        mutableEvents.update { current ->
            current.map { event ->
                if (event.id == eventId) event.copy(status = ValidationStatus.REJECTED) else event
            }
        }
    }
}
