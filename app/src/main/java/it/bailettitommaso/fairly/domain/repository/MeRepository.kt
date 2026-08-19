package it.bailettitommaso.fairly.domain.repository

import it.bailettitommaso.fairly.domain.model.User

/** Outcome of a `PUT /me` password change. */
sealed interface ChangePasswordResult {
    data object Success : ChangePasswordResult

    /** Backend rejected the current password (HTTP 422). */
    data object InvalidCurrentPassword : ChangePasswordResult

    /** Could not reach the backend. */
    data object Offline : ChangePasswordResult

    /** Any other failure (unexpected status, malformed body). */
    data object Error : ChangePasswordResult
}

/** Outcome of a `PUT /me` profile update. */
sealed interface UpdateProfileResult {
    data class Success(val user: User) : UpdateProfileResult

    /** Could not reach the backend. */
    data object Offline : UpdateProfileResult

    /** Any other failure (validation rejection, unexpected status, malformed body). */
    data object Error : UpdateProfileResult
}

interface MeRepository {
    /** [currentPassword] is required unless the account is under a forced `must_change_password` reset. */
    suspend fun changePassword(currentPassword: String?, password: String, passwordConfirmation: String): ChangePasswordResult

    suspend fun updateName(name: String): UpdateProfileResult
}
