package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safelock_prefs")

class SafeLockPreferences(private val context: Context) {

    companion object {
        val KEY_STUDENT_ID = stringPreferencesKey("student_id")
        val KEY_STUDENT_NAME = stringPreferencesKey("student_name")
        val KEY_WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val KEY_WALLPAPER_CUSTOM_URI = stringPreferencesKey("wallpaper_custom_uri")
        val KEY_ONBOARDING_COMPLETED = stringPreferencesKey("onboarding_completed")

        fun generateStudentId(): String {
            val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // avoids ambiguous chars like 0, O, 1, I
            val random = SecureRandom()
            val suffix = (1..5).map { chars[random.nextInt(chars.length)] }.joinToString("")
            return "SL-$suffix"
        }
    }

    val studentIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_STUDENT_ID]
    }

    val wallpaperPresetFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WALLPAPER_PRESET] ?: "cyber_dark"
    }

    val wallpaperCustomUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_WALLPAPER_CUSTOM_URI]
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED]?.toBoolean() ?: false
    }

    suspend fun getOrCreateStudentId(): String {
        val current = context.dataStore.data.first()[KEY_STUDENT_ID]
        if (!current.isNullOrBlank()) {
            return current
        }
        val newId = generateStudentId()
        context.dataStore.edit { prefs ->
            prefs[KEY_STUDENT_ID] = newId
        }
        return newId
    }

    suspend fun saveStudentId(studentId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STUDENT_ID] = studentId
        }
    }

    suspend fun saveWallpaperPreset(presetId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_PRESET] = presetId
            prefs.remove(KEY_WALLPAPER_CUSTOM_URI)
        }
    }

    suspend fun saveCustomWallpaperUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_CUSTOM_URI] = uri
            prefs[KEY_WALLPAPER_PRESET] = "custom"
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed.toString()
        }
    }
}
