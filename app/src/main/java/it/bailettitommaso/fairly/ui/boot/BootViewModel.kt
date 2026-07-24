package it.bailettitommaso.fairly.ui.boot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.model.User
import it.bailettitommaso.fairly.domain.repository.SessionRepository
import it.bailettitommaso.fairly.domain.repository.SessionResult
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
    data object Offline : BootState
}

@HiltViewModel
class BootViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow<BootState>(BootState.Loading)
    val state: StateFlow<BootState> = _state.asStateFlow()

    init {
        check()
        observeSessionExpiry()
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

    /** Re-runs the boot session check (used by the offline screen on reconnect). */
    fun retry() {
        Timber.d("boot: retry requested")
        check()
    }

    private fun check() {
        _state.value = BootState.Loading
        viewModelScope.launch {
            val newState = when (val result = sessionRepository.bootstrap()) {
                is SessionResult.Authenticated -> {
                    sessionManager.markAuthenticated()
                    BootState.Authenticated(result.user)
                }
                SessionResult.Unauthenticated -> BootState.Unauthenticated
                SessionResult.Offline -> BootState.Offline
            }
            Timber.d("boot: resolved state=%s", newState::class.simpleName)
            _state.value = newState
        }
    }
}
