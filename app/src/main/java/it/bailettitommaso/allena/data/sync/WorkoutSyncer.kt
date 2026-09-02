package it.bailettitommaso.allena.data.sync

import it.bailettitommaso.allena.data.local.TokenStore
import it.bailettitommaso.allena.di.ApplicationScope
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import it.bailettitommaso.allena.util.ConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads workouts that were finished while offline.
 *
 * Lives for the whole process rather than on a screen: a workout logged on a dead connection has
 * to reach the server even if the user never opens the history screen again.
 */
@Singleton
class WorkoutSyncer @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val tokenStore: TokenStore,
    private val connectivityObserver: ConnectivityObserver,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val mutex = Mutex()

    /**
     * Retries on every reported connection, including the seeded current one — unlike the offline
     * screen, the state at startup is exactly when a workout from a previous session should go up.
     */
    fun start() {
        scope.launch {
            connectivityObserver.status.collect { status ->
                if (status == ConnectivityObserver.Status.AVAILABLE) {
                    syncNow()
                }
            }
        }
    }

    /** Uploading needs a bearer token; without one an attempt would 401 and force a logout. */
    suspend fun syncNow() {
        if (tokenStore.currentToken() == null) return

        // Connectivity can flap; one upload pass at a time avoids sending a session twice.
        if (!mutex.tryLock()) return
        try {
            val uploaded = workoutRepository.syncPending()
            if (uploaded > 0) {
                Timber.d("workout sync: uploaded %d pending session(s)", uploaded)
            }
        } finally {
            mutex.unlock()
        }
    }
}
