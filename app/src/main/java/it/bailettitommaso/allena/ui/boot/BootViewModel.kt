package it.bailettitommaso.allena.ui.boot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.data.session.SessionManager
import it.bailettitommaso.allena.domain.model.User
import it.bailettitommaso.allena.domain.repository.SessionRepository
import it.bailettitommaso.allena.domain.repository.SessionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface BootState {
    data object Loading : BootState
    data class Authenticated(val user: User) : BootState
    data object Unauthenticated : BootState
    data class Unreachable(val cause: SessionResult.Unreachable.Cause) : BootState
}

@HiltViewModel
class BootViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow<BootState>(BootState.Loading)
    val state: StateFlow<BootState> = _state.asStateFlow()

    private val _retrying = MutableStateFlow(false)
    val retrying: StateFlow<Boolean> = _retrying.asStateFlow()

    private var routedState: BootState? = null

    init {
        viewModelScope.launch { resolve() }
        observeSessionExpiry()
    }

    /**
     * True only the first time the nav graph sees a given state.
     *
     * This ViewModel outlives the activity, so recreating it (a rotation, a system theme change)
     * replays the routing effect with a state that has not actually changed. Acting on it again
     * sends the user back to the start destination and clears the restored back stack, which looks
     * like being thrown out of a screen mid-task.
     */
    fun shouldRoute(state: BootState): Boolean {
        if (routedState == state) return false

        routedState = state

        return true
    }

    /** A `401` on any authenticated call funnels here via [SessionManager], routing back to login. */
    private fun observeSessionExpiry() {
        viewModelScope.launch {
            sessionManager.sessionExpired.filter { it }.collect {
                Timber.d("boot: session expired, routing to login")
                _state.value = BootState.Unauthenticated
            }
        }
    }

    /**
     * Re-runs the boot session check (used by the offline screen on reconnect). Deliberately keeps
     * the current state instead of flipping back to [BootState.Loading]: the nav graph keys off
     * [state], and the extra transition used to re-trigger navigation and loop.
     */
    fun retry() {
        if (_retrying.value) return
        Timber.d("boot: retry requested")
        _retrying.value = true
        viewModelScope.launch {
            resolve()
            _retrying.value = false
        }
    }

    private suspend fun resolve() {
        val newState = when (val result = sessionRepository.bootstrap()) {
            is SessionResult.Authenticated -> {
                sessionManager.markAuthenticated()
                BootState.Authenticated(result.user)
            }
            SessionResult.Unauthenticated -> BootState.Unauthenticated
            is SessionResult.Unreachable -> BootState.Unreachable(result.cause)
        }
        Timber.d("boot: resolved state=%s", newState::class.simpleName)
        _state.value = newState
    }
}
