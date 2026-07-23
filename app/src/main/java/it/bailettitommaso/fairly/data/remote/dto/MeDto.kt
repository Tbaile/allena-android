package it.bailettitommaso.fairly.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /me` wraps the payload in a top-level `data` key (Laravel JsonResource). */
@Serializable
data class MeEnvelopeDto(
    val data: MeDto,
)

@Serializable
data class MeDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String? = null,
    @SerialName("must_change_password") val mustChangePassword: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)
