package it.bailettitommaso.allena.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val mustChangePassword: Boolean,
    val avatarUrl: String? = null,
)
