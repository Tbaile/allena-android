package it.bailettitommaso.allena.data.repository

import it.bailettitommaso.allena.data.local.TokenStore
import it.bailettitommaso.allena.data.remote.api.MeApi
import it.bailettitommaso.allena.data.remote.dto.toDomain
import it.bailettitommaso.allena.domain.repository.SessionRepository
import it.bailettitommaso.allena.domain.repository.SessionResult
import it.bailettitommaso.allena.util.ConnectivityObserver
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val tokenStore: TokenStore,
    private val meApi: MeApi,
    private val connectivityObserver: ConnectivityObserver,
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
            Timber.d("bootstrap: authenticated")
            SessionResult.Authenticated(user)
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNAUTHORIZED) {
                Timber.d("bootstrap: token rejected (401), clearing")
                tokenStore.clear()
                SessionResult.Unauthenticated
            } else {
                // Unexpected server error — treat as transient so the user can retry.
                Timber.d("bootstrap: server error %d", e.code())
                SessionResult.Unreachable(SessionResult.Unreachable.Cause.SERVER)
            }
        } catch (e: IOException) {
            // A refused/timed-out connection while the device is online means the backend is down.
            val cause = if (connectivityObserver.isOnline()) {
                SessionResult.Unreachable.Cause.SERVER
            } else {
                SessionResult.Unreachable.Cause.NETWORK
            }
            Timber.d(e, "bootstrap: unreachable, cause=%s", cause)
            SessionResult.Unreachable(cause)
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
