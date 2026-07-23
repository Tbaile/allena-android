package it.bailettitommaso.fairly.domain.repository

import it.bailettitommaso.fairly.domain.model.User

/** Outcome of the boot-time session check. */
sealed interface SessionResult {
    /** Valid token, `GET /me` succeeded. */
    data class Authenticated(val user: User) : SessionResult

    /** No token stored, or the token was rejected (401) and has been cleared. */
    data object Unauthenticated : SessionResult

    /** Could not reach the backend (no connectivity / transient network error). */
    data object Offline : SessionResult
}

interface SessionRepository {
    /**
     * Resolves the current session: reads the stored token and, if present,
     * validates it against `GET /me`. Clears the token on a 401.
     */
    suspend fun bootstrap(): SessionResult
}
