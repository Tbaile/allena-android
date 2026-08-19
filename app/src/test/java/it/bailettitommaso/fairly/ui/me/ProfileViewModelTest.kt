package it.bailettitommaso.fairly.ui.me

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.MainDispatcherRule
import it.bailettitommaso.fairly.data.session.CurrentUserStore
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.model.User
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import it.bailettitommaso.fairly.domain.repository.MeRepository
import it.bailettitommaso.fairly.domain.repository.UpdateProfileResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val meRepository = mockk<MeRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val currentUserStore = mockk<CurrentUserStore>()

    private val user = User(id = 1, name = "Mario", email = "mario@example.com", mustChangePassword = false)

    private fun viewModel(): ProfileViewModel {
        coEvery { currentUserStore.refresh() } returns user
        return ProfileViewModel(
            authRepository = authRepository,
            meRepository = meRepository,
            sessionManager = sessionManager,
            currentUserStore = currentUserStore,
        )
    }

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
}
