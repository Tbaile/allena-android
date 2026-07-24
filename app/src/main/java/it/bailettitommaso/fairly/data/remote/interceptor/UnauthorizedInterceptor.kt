package it.bailettitommaso.fairly.data.remote.interceptor

import it.bailettitommaso.fairly.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Response-side interceptor that funnels every `401 Unauthorized` into [SessionManager]. Uses an
 * interceptor rather than an OkHttp `Authenticator` because Sanctum returns a JSON `401` without a
 * `WWW-Authenticate` challenge and we never want an automatic re-auth/retry.
 */
class UnauthorizedInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == HTTP_UNAUTHORIZED) {
            sessionManager.onUnauthorized()
        }
        return response
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
