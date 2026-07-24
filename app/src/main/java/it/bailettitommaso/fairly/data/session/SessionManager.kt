package it.bailettitommaso.fairly.data.session

import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide choke point for session expiry. A `401` raised by any authenticated call (surfaced by
 * [it.bailettitommaso.fairly.data.remote.interceptor.UnauthorizedInterceptor]) funnels through
 * [onUnauthorized]; the resulting [sessionExpired] flag drives the app back to login with a flash.
 *
 * The [wasAuthenticated] gate ensures the flash fires only for a session that breaks *while in use*
 * — a cold-start with a stale token never calls [markAuthenticated], so its boot `401` is a no-op
 * here and the user just lands on a clean login screen.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val wasAuthenticated = AtomicBoolean(false)

    private val _sessionExpired = MutableStateFlow(false)

    /** Sticky flag: `true` once an active session is rejected, until the login screen consumes it. */
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    /** Arms the gate: the user now has a live session worth flashing about if it later breaks. */
    fun markAuthenticated() {
        wasAuthenticated.set(true)
    }

    /** Closes the gate on a deliberate logout so a racing `401` cannot produce a false flash. */
    fun markLoggedOut() {
        wasAuthenticated.set(false)
    }

    /**
     * Called (possibly off the main thread, possibly concurrently) when a `401` is observed.
     * The [compareAndSet] both enforces the "was authenticated" gate and makes a parallel-`401`
     * storm idempotent — only the first caller expires the session.
     */
    fun onUnauthorized() {
        if (!wasAuthenticated.compareAndSet(true, false)) return
        Timber.d("session: token rejected mid-session (401), expiring")
        scope.launch { tokenStore.clear() }
        _sessionExpired.value = true
    }

    /** Resets the flag once the login screen has shown the "session expired" flash. */
    fun consumeSessionExpired() {
        _sessionExpired.value = false
    }
}
