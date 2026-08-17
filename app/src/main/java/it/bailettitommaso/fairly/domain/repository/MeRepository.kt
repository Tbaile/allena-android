package it.bailettitommaso.fairly.domain.repository

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

interface MeRepository {
    /** [currentPassword] is required unless the account is under a forced `must_change_password` reset. */
    suspend fun changePassword(currentPassword: String?, password: String, passwordConfirmation: String): ChangePasswordResult
}
