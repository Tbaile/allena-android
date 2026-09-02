package it.bailettitommaso.allena.data.remote.interceptor

import it.bailettitommaso.allena.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches `Authorization: Bearer <token>` to every request when a token is available. */
class BearerTokenInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Fast path: in-memory mirror. Fall back to disk on a cold cache.
        val token = tokenStore.cachedTokenOrNull() ?: runBlocking { tokenStore.currentToken() }
        val request = chain.request()
        val authed = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(authed)
    }
}
