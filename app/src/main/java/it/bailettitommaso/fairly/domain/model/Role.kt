package it.bailettitommaso.fairly.domain.model

/**
 * Role assigned to a user by the backend (Spatie roles, exposed as a single string).
 * [UNKNOWN] covers a null/unrecognised value so callers never have to handle nulls.
 */
enum class Role {
    ADMIN,
    EXPERT,
    CLIENT,
    UNKNOWN,
    ;

    companion object {
        fun fromApi(value: String?): Role = when (value) {
            "admin" -> ADMIN
            "expert" -> EXPERT
            "client" -> CLIENT
            else -> UNKNOWN
        }
    }
}
