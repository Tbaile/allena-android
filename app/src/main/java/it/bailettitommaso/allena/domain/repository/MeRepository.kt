package it.bailettitommaso.allena.domain.repository

import it.bailettitommaso.allena.domain.model.User
import java.io.File

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

/** Outcome of an avatar upload or removal. */
sealed interface AvatarResult {
    data class Success(val user: User) : AvatarResult

    /** Backend refused the file: wrong type or over the size limit (HTTP 422). */
    data object Rejected : AvatarResult

    /** Could not reach the backend. */
    data object Offline : AvatarResult

    /** Any other failure (unexpected status, malformed body). */
    data object Error : AvatarResult
}

interface MeRepository {
    /** [currentPassword] is required unless the account is under a forced `must_change_password` reset. */
    suspend fun changePassword(currentPassword: String?, password: String, passwordConfirmation: String): ChangePasswordResult

    suspend fun updateName(name: String): UpdateProfileResult

    suspend fun uploadAvatar(photo: File): AvatarResult

    suspend fun removeAvatar(): AvatarResult
}
