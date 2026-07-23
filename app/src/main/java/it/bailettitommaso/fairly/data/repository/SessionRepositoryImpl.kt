package it.bailettitommaso.fairly.data.repository

import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.repository.SessionRepository
import it.bailettitommaso.fairly.domain.repository.SessionResult
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val tokenStore: TokenStore,
    private val meApi: MeApi,
) : SessionRepository {
    override suspend fun bootstrap(): SessionResult {
        // Warm the token mirror so the interceptor can attach it.
        val token = tokenStore.currentToken()
        if (token.isNullOrBlank()) {
            Timber.d("bootstrap: no stored token, unauthenticated")
            return SessionResult.Unauthenticated
        }

        return try {
            val user = meApi.me().data.toDomain()
            Timber.d("bootstrap: authenticated as role=%s", user.role)
            SessionResult.Authenticated(user)
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNAUTHORIZED) {
                Timber.d("bootstrap: token rejected (401), clearing")
                tokenStore.clear()
                SessionResult.Unauthenticated
            } else {
                // Unexpected server error — treat as transient so the user can retry.
                Timber.d("bootstrap: server error %d, treating as offline", e.code())
                SessionResult.Offline
            }
        } catch (e: IOException) {
            Timber.d(e, "bootstrap: network error, treating as offline")
            SessionResult.Offline
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
