package it.bailettitommaso.allena.domain.repository

import it.bailettitommaso.allena.domain.model.User

/** Outcome of the boot-time session check. */
sealed interface SessionResult {
    /** Valid token, `GET /me` succeeded. */
    data class Authenticated(val user: User) : SessionResult

    /** No token stored, or the token was rejected (401) and has been cleared. */
    data object Unauthenticated : SessionResult

    /** Could not reach the backend. [cause] separates a dead network from a dead/erroring server. */
    data class Unreachable(val cause: Cause) : SessionResult {
        enum class Cause { NETWORK, SERVER }
    }
}

interface SessionRepository {
    /**
     * Resolves the current session: reads the stored token and, if present,
     * validates it against `GET /me`. Clears the token on a 401.
     */
    suspend fun bootstrap(): SessionResult
}
