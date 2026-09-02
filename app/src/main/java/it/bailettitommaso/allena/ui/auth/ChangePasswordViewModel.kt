package it.bailettitommaso.allena.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.data.session.CurrentUserStore
import it.bailettitommaso.allena.domain.repository.ChangePasswordResult
import it.bailettitommaso.allena.domain.repository.MeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_PASSWORD_LENGTH = 8

enum class ChangePasswordError { INVALID_CURRENT_PASSWORD, TOO_SHORT, MISMATCH, OFFLINE, GENERIC }

data class ChangePasswordUiState(
    val forced: Boolean = false,
    val currentPassword: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val isSubmitting: Boolean = false,
    val error: ChangePasswordError? = null,
    val done: Boolean = false,
) {
    val requireCurrentPassword: Boolean
        get() = !forced

    val canSubmit: Boolean
        get() = !isSubmitting && password.isNotBlank() && passwordConfirmation.isNotBlank() &&
            (!requireCurrentPassword || currentPassword.isNotBlank())
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meRepository: MeRepository,
    private val currentUserStore: CurrentUserStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChangePasswordUiState(forced = savedStateHandle.get<Boolean>("forced") ?: false),
    )
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun onCurrentPasswordChange(value: String) =
        _state.update { it.copy(currentPassword = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun onPasswordConfirmationChange(value: String) =
        _state.update { it.copy(passwordConfirmation = value, error = null) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        if (current.password.length < MIN_PASSWORD_LENGTH) {
            _state.update { it.copy(error = ChangePasswordError.TOO_SHORT) }
            return
        }
        if (current.password != current.passwordConfirmation) {
            _state.update { it.copy(error = ChangePasswordError.MISMATCH) }
            return
        }

        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = meRepository.changePassword(
                currentPassword = current.currentPassword.takeIf { current.requireCurrentPassword },
                password = current.password,
                passwordConfirmation = current.passwordConfirmation,
            )
            when (result) {
                ChangePasswordResult.Success -> {
                    currentUserStore.refresh()
                    _state.update { it.copy(isSubmitting = false, done = true) }
                }
                ChangePasswordResult.InvalidCurrentPassword ->
                    _state.update { it.copy(isSubmitting = false, error = ChangePasswordError.INVALID_CURRENT_PASSWORD) }
                ChangePasswordResult.Offline ->
                    _state.update { it.copy(isSubmitting = false, error = ChangePasswordError.OFFLINE) }
                ChangePasswordResult.Error ->
                    _state.update { it.copy(isSubmitting = false, error = ChangePasswordError.GENERIC) }
            }
        }
    }
}
