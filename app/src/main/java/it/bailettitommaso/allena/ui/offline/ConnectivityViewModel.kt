package it.bailettitommaso.allena.ui.offline

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.util.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    observer: ConnectivityObserver,
) : ViewModel() {
    /**
     * Emits once per genuine reconnection. The first emission is the seeded current state, not a
     * transition — dropping it is what stops the offline screen from retrying in a tight loop.
     */
    val reconnected: Flow<Unit> = observer.status
        .drop(1)
        .filter { it == ConnectivityObserver.Status.AVAILABLE }
        .map { }
}
