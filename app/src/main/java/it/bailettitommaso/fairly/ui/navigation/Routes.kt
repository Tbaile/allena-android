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

    @Serializable
    data object Home : Route
}
