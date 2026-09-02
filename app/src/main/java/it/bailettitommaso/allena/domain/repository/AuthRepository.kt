package it.bailettitommaso.allena.domain.repository

import it.bailettitommaso.allena.domain.model.User

/** Outcome of a login attempt. */
sealed interface LoginResult {
    data class Success(val user: User) : LoginResult

    /** Backend rejected the credentials (HTTP 422). */
    data object InvalidCredentials : LoginResult

    /** Could not reach the backend. */
    data object Offline : LoginResult

    /** Any other failure (unexpected status, malformed body). */
    data object Error : LoginResult
}

interface AuthRepository {
    /** Authenticates with email/password and, on success, persists the bearer token. */
    suspend fun login(email: String, password: String): LoginResult

    /** Revokes the token server-side and clears it locally. Local state is cleared regardless of server outcome. */
    suspend fun logout()
}
