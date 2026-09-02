package it.bailettitommaso.allena.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/** Attaches `Accept: application/json` to every request so Laravel returns JSON errors instead of redirects. */
class AcceptJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
