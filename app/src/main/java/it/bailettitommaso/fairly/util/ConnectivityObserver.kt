package it.bailettitommaso.fairly.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/** Emits connectivity changes so screens can react to the network coming back. */
class ConnectivityObserver(context: Context) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()

    enum class Status { AVAILABLE, UNAVAILABLE }

    val status: Flow<Status> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(Status.UNAVAILABLE)
            awaitClose { }
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("connectivity: available")
                trySend(Status.AVAILABLE)
            }

            override fun onLost(network: Network) {
                Timber.d("connectivity: lost")
                trySend(Status.UNAVAILABLE)
            }
        }

        // Seed with the current state so collectors don't wait for the next change.
        val initial = if (manager.hasInternet()) Status.AVAILABLE else Status.UNAVAILABLE
        Timber.d("connectivity: initial=%s", initial)
        trySend(initial)
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /** Point-in-time check, for callers that can't collect [status]. */
    fun isOnline(): Boolean = connectivityManager?.hasInternet() == true

    private fun ConnectivityManager.hasInternet(): Boolean {
        val caps = getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
