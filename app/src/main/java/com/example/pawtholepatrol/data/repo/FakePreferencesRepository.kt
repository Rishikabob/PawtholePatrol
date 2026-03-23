package com.example.pawtholepatrol.data.repo

import com.example.pawtholepatrol.core.model.UserPrefs
import com.example.pawtholepatrol.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePreferencesRepository : PreferencesRepository {
    private val prefsState = MutableStateFlow(UserPrefs())

    override fun observeUserPrefs(): Flow<UserPrefs> = prefsState.asStateFlow()

    override suspend fun updateUserPrefs(prefs: UserPrefs) {
        prefsState.value = prefs
    }
}
