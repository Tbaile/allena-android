package it.bailettitommaso.fairly.data.session

import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.model.User
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Fetches the authenticated user from `GET /me`. Used by login to resolve `must_change_password`. */
@Singleton
class CurrentUserStore @Inject constructor(
    private val meApi: MeApi,
) {
    /** Fetches `GET /me` and maps it; null if the call fails. */
    suspend fun refresh(): User? = try {
        meApi.me().data.toDomain()
    } catch (e: Exception) {
        Timber.d(e, "currentUser: refresh failed")
        null
    }
}
