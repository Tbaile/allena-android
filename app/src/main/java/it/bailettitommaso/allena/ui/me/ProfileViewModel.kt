package it.bailettitommaso.allena.ui.me

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.data.session.CurrentUserStore
import it.bailettitommaso.allena.data.session.SessionManager
import it.bailettitommaso.allena.domain.repository.AuthRepository
import it.bailettitommaso.allena.domain.repository.AvatarResult
import it.bailettitommaso.allena.domain.repository.MeRepository
import it.bailettitommaso.allena.domain.repository.UpdateProfileResult
import it.bailettitommaso.allena.util.ImageFileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ProfileError { OFFLINE, GENERIC, PHOTO_REJECTED }

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val isEditing: Boolean = false,
    val nameDraft: String = "",
    val isSaving: Boolean = false,
    val avatarUrl: String? = null,
    val isAvatarBusy: Boolean = false,
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
    private val imageFileStore: ImageFileStore,
) : ViewModel() {

    /** The file the camera was told to write into, kept until the capture result comes back. */
    private var pendingCapture: File? = null

    private val _profile = MutableStateFlow(ProfileUiState())
    val profile: StateFlow<ProfileUiState> = _profile.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init {
        viewModelScope.launch {
            val user = currentUserStore.refresh()
            _profile.value = if (user != null) {
                ProfileUiState(
                    isLoading = false,
                    name = user.name,
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                )
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

    /** Creates the destination file and hands the camera a content URI it is allowed to write to. */
    fun prepareCaptureUri(): Uri {
        val file = imageFileStore.newCaptureFile()
        pendingCapture = file
        return imageFileStore.uriFor(file)
    }

    fun onCaptureResult(saved: Boolean) {
        val file = pendingCapture
        pendingCapture = null

        if (!saved || file == null) {
            file?.delete()
            return
        }

        _profile.update { it.copy(isAvatarBusy = true, error = null) }
        viewModelScope.launch { upload(file) }
    }

    fun onPhotoPicked(uri: Uri) {
        _profile.update { it.copy(isAvatarBusy = true, error = null) }
        viewModelScope.launch {
            val file = imageFileStore.copyToCache(uri)
            if (file == null) {
                _profile.update { it.copy(isAvatarBusy = false, error = ProfileError.GENERIC) }
            } else {
                upload(file)
            }
        }
    }

    fun removeAvatar() {
        _profile.update { it.copy(isAvatarBusy = true, error = null) }
        viewModelScope.launch { applyAvatarResult(meRepository.removeAvatar()) }
    }

    private suspend fun upload(file: File) {
        val result = meRepository.uploadAvatar(file)
        file.delete()
        applyAvatarResult(result)
    }

    private suspend fun applyAvatarResult(result: AvatarResult) {
        when (result) {
            is AvatarResult.Success -> {
                currentUserStore.refresh()
                _profile.update { it.copy(isAvatarBusy = false, avatarUrl = result.user.avatarUrl) }
            }
            AvatarResult.Rejected ->
                _profile.update { it.copy(isAvatarBusy = false, error = ProfileError.PHOTO_REJECTED) }
            AvatarResult.Offline ->
                _profile.update { it.copy(isAvatarBusy = false, error = ProfileError.OFFLINE) }
            AvatarResult.Error ->
                _profile.update { it.copy(isAvatarBusy = false, error = ProfileError.GENERIC) }
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
