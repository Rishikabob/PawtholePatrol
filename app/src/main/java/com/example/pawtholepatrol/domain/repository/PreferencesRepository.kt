package com.example.pawtholepatrol.domain.repository

import com.example.pawtholepatrol.core.model.UserPrefs
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeUserPrefs(): Flow<UserPrefs>
    suspend fun updateUserPrefs(prefs: UserPrefs)
}
