package it.bailettitommaso.fairly.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
sealed interface Route {
    @Serializable
    data object Boot : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Offline : Route

    /** [forced] true after a login where `must_change_password` is set: no back nav, no current-password field. */
    @Serializable
    data class ChangePassword(val forced: Boolean = false) : Route

    @Serializable
    data object Home : Route

    @Serializable
    data class ExerciseDetail(val id: Long) : Route
}
