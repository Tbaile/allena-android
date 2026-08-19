package it.bailettitommaso.fairly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.bailettitommaso.fairly.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the user's light/dark/system theme choice in DataStore. */
@Singleton
class ThemeStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val mode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE]
            ?.let { stored -> runCatching { ThemeMode.valueOf(stored) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
