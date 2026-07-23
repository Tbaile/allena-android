package it.bailettitommaso.fairly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

/** `POST /auth/login` response. Note: the user is NOT wrapped in a `data` envelope here. */
@Serializable
data class LoginResponseDto(
    val token: String,
    val user: UserDto,
)

/** Lean user shape returned by login (no `must_change_password` / `created_at`). */
@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String? = null,
)
