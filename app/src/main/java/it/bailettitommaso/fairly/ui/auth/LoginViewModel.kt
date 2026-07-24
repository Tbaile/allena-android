package it.bailettitommaso.fairly.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import it.bailettitommaso.fairly.domain.repository.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginError { INVALID_CREDENTIALS, OFFLINE, GENERIC }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: LoginError? = null,
    val loggedIn: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && email.isNotBlank() && password.isNotBlank()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Sticky flag mirrored from [SessionManager]; the screen flashes and then [consumeSessionExpired]s. */
    val sessionExpired: StateFlow<Boolean> = sessionManager.sessionExpired

    fun consumeSessionExpired() = sessionManager.consumeSessionExpired()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.login(current.email.trim(), current.password)
            _state.update {
                when (result) {
                    is LoginResult.Success -> {
                        sessionManager.markAuthenticated()
                        it.copy(isSubmitting = false, loggedIn = true)
                    }
                    LoginResult.InvalidCredentials ->
                        it.copy(isSubmitting = false, error = LoginError.INVALID_CREDENTIALS)
                    LoginResult.Offline ->
                        it.copy(isSubmitting = false, error = LoginError.OFFLINE)
                    LoginResult.Error ->
                        it.copy(isSubmitting = false, error = LoginError.GENERIC)
                }
            }
        }
    }
}
