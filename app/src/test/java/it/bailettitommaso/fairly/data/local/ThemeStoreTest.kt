package it.bailettitommaso.fairly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.bailettitommaso.fairly.domain.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ThemeStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `defaults to system when nothing is stored`() = runTest {
        val store = ThemeStore(dataStore(backgroundScope))

        assertEquals(ThemeMode.SYSTEM, store.mode.first())
    }

    @Test
    fun `set mode persists the choice`() = runTest {
        val store = ThemeStore(dataStore(backgroundScope))

        store.setMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, store.mode.first())
    }

    @Test
    fun `falls back to system when the stored value is unknown`() = runTest {
        val dataStore = dataStore(backgroundScope)
        dataStore.edit { it[stringPreferencesKey("theme_mode")] = "SEPIA" }

        assertEquals(ThemeMode.SYSTEM, ThemeStore(dataStore).mode.first())
    }

    private fun dataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(folder.root, "theme.preferences_pb")
        }
}
