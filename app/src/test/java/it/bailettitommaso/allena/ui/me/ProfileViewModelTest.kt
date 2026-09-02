package it.bailettitommaso.allena.ui.me

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.data.session.CurrentUserStore
import it.bailettitommaso.allena.data.session.SessionManager
import it.bailettitommaso.allena.domain.model.User
import it.bailettitommaso.allena.domain.repository.AuthRepository
import it.bailettitommaso.allena.domain.repository.AvatarResult
import it.bailettitommaso.allena.domain.repository.MeRepository
import it.bailettitommaso.allena.domain.repository.UpdateProfileResult
import it.bailettitommaso.allena.util.ImageFileStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val meRepository = mockk<MeRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val currentUserStore = mockk<CurrentUserStore>()
    private val imageFileStore = mockk<ImageFileStore>()

    private val user = User(id = 1, name = "Mario", email = "mario@example.com", mustChangePassword = false)

    private fun viewModel(): ProfileViewModel {
        coEvery { currentUserStore.refresh() } returns user
        return ProfileViewModel(
            authRepository = authRepository,
            meRepository = meRepository,
            sessionManager = sessionManager,
            currentUserStore = currentUserStore,
            imageFileStore = imageFileStore,
        )
    }

    private fun tempPhoto(): File = File.createTempFile("avatar", ".jpg").apply { deleteOnExit() }

    @Test
    fun `save applies the updated name and leaves edit mode`() = runTest {
        val viewModel = viewModel()
        coEvery { meRepository.updateName("Mario Rossi") } returns
            UpdateProfileResult.Success(user.copy(name = "Mario Rossi"))

        viewModel.startEdit()
        viewModel.onNameChange("Mario Rossi")
        viewModel.save()

        val state = viewModel.profile.value
        assertEquals("Mario Rossi", state.name)
        assertFalse(state.isEditing)
        assertFalse(state.isSaving)
        assertNull(state.error)
    }

    @Test
    fun `save failing offline keeps the draft and reports the error`() = runTest {
        val viewModel = viewModel()
        coEvery { meRepository.updateName(any()) } returns UpdateProfileResult.Offline

        viewModel.startEdit()
        viewModel.onNameChange("Mario Rossi")
        viewModel.save()

        val state = viewModel.profile.value
        assertEquals(ProfileError.OFFLINE, state.error)
        assertTrue(state.isEditing)
        assertEquals("Mario Rossi", state.nameDraft)
        assertEquals("Mario", state.name)
    }

    @Test
    fun `save is a no-op for a blank or unchanged name`() = runTest {
        val viewModel = viewModel()

        viewModel.startEdit()
        viewModel.onNameChange("   ")
        viewModel.save()
        assertFalse(viewModel.profile.value.canSave)

        viewModel.onNameChange("Mario")
        viewModel.save()

        assertFalse(viewModel.profile.value.canSave)
        coVerify(exactly = 0) { meRepository.updateName(any()) }
    }

    @Test
    fun `a captured photo is uploaded and becomes the new avatar`() = runTest {
        val viewModel = viewModel()
        val photo = tempPhoto()
        every { imageFileStore.newCaptureFile() } returns photo
        every { imageFileStore.uriFor(photo) } returns mockk()
        coEvery { meRepository.uploadAvatar(photo) } returns
            AvatarResult.Success(user.copy(avatarUrl = "http://host/api/v1/me/avatar?v=abc"))

        viewModel.prepareCaptureUri()
        viewModel.onCaptureResult(saved = true)

        val state = viewModel.profile.value
        assertEquals("http://host/api/v1/me/avatar?v=abc", state.avatarUrl)
        assertFalse(state.isAvatarBusy)
        assertNull(state.error)
    }

    @Test
    fun `a cancelled capture uploads nothing`() = runTest {
        val viewModel = viewModel()
        val photo = tempPhoto()
        every { imageFileStore.newCaptureFile() } returns photo
        every { imageFileStore.uriFor(photo) } returns mockk()

        viewModel.prepareCaptureUri()
        viewModel.onCaptureResult(saved = false)

        coVerify(exactly = 0) { meRepository.uploadAvatar(any()) }
        assertFalse(viewModel.profile.value.isAvatarBusy)
    }

    @Test
    fun `a picked photo that cannot be read surfaces a generic error`() = runTest {
        val viewModel = viewModel()
        val uri = mockk<Uri>()
        coEvery { imageFileStore.copyToCache(uri) } returns null

        viewModel.onPhotoPicked(uri)

        val state = viewModel.profile.value
        assertEquals(ProfileError.GENERIC, state.error)
        assertFalse(state.isAvatarBusy)
        coVerify(exactly = 0) { meRepository.uploadAvatar(any()) }
    }

    @Test
    fun `a rejected upload surfaces the photo error`() = runTest {
        val viewModel = viewModel()
        val uri = mockk<Uri>()
        val photo = tempPhoto()
        coEvery { imageFileStore.copyToCache(uri) } returns photo
        coEvery { meRepository.uploadAvatar(photo) } returns AvatarResult.Rejected

        viewModel.onPhotoPicked(uri)

        assertEquals(ProfileError.PHOTO_REJECTED, viewModel.profile.value.error)
    }

    @Test
    fun `removing the avatar clears the url`() = runTest {
        val viewModel = viewModel()
        coEvery { meRepository.removeAvatar() } returns AvatarResult.Success(user.copy(avatarUrl = null))

        viewModel.removeAvatar()

        val state = viewModel.profile.value
        assertNull(state.avatarUrl)
        assertFalse(state.isAvatarBusy)
    }
}
