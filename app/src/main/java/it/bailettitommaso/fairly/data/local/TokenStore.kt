package it.bailettitommaso.fairly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the Sanctum bearer token in DataStore. Also keeps a `@Volatile` mirror so the
 * OkHttp [it.bailettitommaso.fairly.data.remote.interceptor.BearerTokenInterceptor] can read
 * it synchronously without blocking on a coroutine per request.
 */
@Singleton
class TokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    @Volatile
    private var cachedToken: String? = null

    /** Returns the token from the in-memory mirror, falling back to disk on first access. */
    suspend fun currentToken(): String? {
        cachedToken?.let { return it }
        return dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()?.also { cachedToken = it }
    }

    /** Synchronous, non-blocking read for the interceptor. Null until [currentToken] has warmed it. */
    fun cachedTokenOrNull(): String? = cachedToken

    suspend fun save(token: String) {
        cachedToken = token
        dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        cachedToken = null
        dataStore.edit { it.remove(KEY_TOKEN) }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("bearer_token")
    }
}
