package it.bailettitommaso.fairly.ui.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.util.ConnectivityObserver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    observer: ConnectivityObserver,
) : ViewModel() {
    val status: StateFlow<ConnectivityObserver.Status> = observer.status.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConnectivityObserver.Status.UNAVAILABLE,
    )
}
