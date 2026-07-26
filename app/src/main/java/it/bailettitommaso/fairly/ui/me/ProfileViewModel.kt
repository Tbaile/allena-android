package it.bailettitommaso.fairly.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.data.session.CurrentUserStore
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val currentUserStore: CurrentUserStore,
) : ViewModel() {

    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserStore.refresh()?.let { user ->
                _profile.value = ProfileUiState(name = user.name, email = user.email)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // Deliberate logout: close the gate so a racing 401 can't flash "session expired".
            sessionManager.markLoggedOut()
            _loggedOut.value = true
        }
    }
}
