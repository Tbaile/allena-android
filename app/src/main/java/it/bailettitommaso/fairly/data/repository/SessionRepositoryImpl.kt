package it.bailettitommaso.fairly.data.repository

import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.repository.SessionRepository
import it.bailettitommaso.fairly.domain.repository.SessionResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val tokenStore: TokenStore,
    private val meApi: MeApi,
) : SessionRepository {
    override suspend fun bootstrap(): SessionResult {
        // Warm the token mirror so the interceptor can attach it.
        val token = tokenStore.currentToken()
        if (token.isNullOrBlank()) return SessionResult.Unauthenticated

        return try {
            val user = meApi.me().data.toDomain()
            SessionResult.Authenticated(user)
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNAUTHORIZED) {
                tokenStore.clear()
                SessionResult.Unauthenticated
            } else {
                // Unexpected server error — treat as transient so the user can retry.
                SessionResult.Offline
            }
        } catch (e: IOException) {
            SessionResult.Offline
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
