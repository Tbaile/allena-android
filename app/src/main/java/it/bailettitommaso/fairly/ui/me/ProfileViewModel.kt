package it.bailettitommaso.fairly.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.data.session.CurrentUserStore
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import it.bailettitommaso.fairly.domain.repository.MeRepository
import it.bailettitommaso.fairly.domain.repository.UpdateProfileResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileError { OFFLINE, GENERIC }

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val isEditing: Boolean = false,
    val nameDraft: String = "",
    val isSaving: Boolean = false,
    val error: ProfileError? = null,
) {
    val canSave: Boolean
        get() = !isSaving && nameDraft.isNotBlank() && nameDraft != name
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val meRepository: MeRepository,
    private val sessionManager: SessionManager,
    private val currentUserStore: CurrentUserStore,
) : ViewModel() {

    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init {
        viewModelScope.launch {
            val user = currentUserStore.refresh()
            _profile.value = if (user != null) {
                ProfileUiState(isLoading = false, name = user.name, email = user.email)
            } else {
                _profile.value.copy(isLoading = false)
            }
        }
    }

    fun startEdit() = _profile.update { it.copy(isEditing = true, nameDraft = it.name, error = null) }

    fun onNameChange(value: String) = _profile.update { it.copy(nameDraft = value, error = null) }

    fun cancelEdit() = _profile.update { it.copy(isEditing = false, nameDraft = "", error = null) }

    fun save() {
        val current = _profile.value
        if (!current.canSave) return

        _profile.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = meRepository.updateName(current.nameDraft.trim())) {
                is UpdateProfileResult.Success -> {
                    currentUserStore.refresh()
                    _profile.update {
                        it.copy(
                            name = result.user.name,
                            email = result.user.email,
                            isEditing = false,
                            nameDraft = "",
                            isSaving = false,
                        )
                    }
                }
                UpdateProfileResult.Offline ->
                    _profile.update { it.copy(isSaving = false, error = ProfileError.OFFLINE) }
                UpdateProfileResult.Error ->
                    _profile.update { it.copy(isSaving = false, error = ProfileError.GENERIC) }
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
