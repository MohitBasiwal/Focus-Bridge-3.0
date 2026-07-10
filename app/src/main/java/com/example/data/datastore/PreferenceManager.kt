package com.example.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "focus_bridge_preferences")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val cooldownMinutesKey = intPreferencesKey("emergency_unlock_cooldown_minutes")
    private val lastUnlockTimeKey = longPreferencesKey("emergency_unlock_last_time")
    private val isOnboardingCompletedKey = booleanPreferencesKey("is_onboarding_completed")
    private val dailyStudyGoalMinutesKey = intPreferencesKey("daily_study_goal_minutes")
    private val dailyUnlockLimitKey = intPreferencesKey("daily_unlock_limit")
    private val notificationEnabledKey = booleanPreferencesKey("notification_enabled")

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isOnboardingCompletedKey] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isOnboardingCompletedKey] = completed
        }
    }

    val dailyStudyGoalMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[dailyStudyGoalMinutesKey] ?: 60
    }

    suspend fun setDailyStudyGoalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[dailyStudyGoalMinutesKey] = minutes
        }
    }

    val dailyUnlockLimit: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[dailyUnlockLimitKey] ?: 3
    }

    suspend fun setDailyUnlockLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[dailyUnlockLimitKey] = limit
        }
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[notificationEnabledKey] ?: true
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationEnabledKey] = enabled
        }
    }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[isDarkModeKey]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDarkModeKey] = enabled
        }
    }

    val cooldownMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[cooldownMinutesKey] ?: 30
    }

    suspend fun setCooldownMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[cooldownMinutesKey] = minutes
        }
    }

    val lastUnlockTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[lastUnlockTimeKey]
    }

    suspend fun setLastUnlockTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[lastUnlockTimeKey] = timestamp
        }
    }
}
