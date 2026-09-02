package it.bailettitommaso.allena.ui.auth

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.data.session.CurrentUserStore
import it.bailettitommaso.allena.domain.repository.ChangePasswordResult
import it.bailettitommaso.allena.domain.repository.MeRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChangePasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val meRepository = mockk<MeRepository>()
    private val currentUserStore = mockk<CurrentUserStore>(relaxed = true)

    private fun viewModel(forced: Boolean) = ChangePasswordViewModel(
        savedStateHandle = SavedStateHandle(mapOf("forced" to forced)),
        meRepository = meRepository,
        currentUserStore = currentUserStore,
    )

    @Test
    fun `forced state does not require the current password`() {
        val viewModel = viewModel(forced = true)

        assertFalse(viewModel.state.value.requireCurrentPassword)
    }

    @Test
    fun `voluntary state requires the current password`() {
        val viewModel = viewModel(forced = false)

        assertTrue(viewModel.state.value.requireCurrentPassword)
    }

    @Test
    fun `submit with a short password sets TOO_SHORT and does not call the repo`() = runTest {
        val viewModel = viewModel(forced = true)
        viewModel.onPasswordChange("short")
        viewModel.onPasswordConfirmationChange("short")

        viewModel.submit()

        assertEquals(ChangePasswordError.TOO_SHORT, viewModel.state.value.error)
        coVerify(exactly = 0) { meRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `submit with mismatched confirmation sets MISMATCH and does not call the repo`() = runTest {
        val viewModel = viewModel(forced = true)
        viewModel.onPasswordChange("newpassword123")
        viewModel.onPasswordConfirmationChange("different123")

        viewModel.submit()

        assertEquals(ChangePasswordError.MISMATCH, viewModel.state.value.error)
        coVerify(exactly = 0) { meRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `blank current password blocks submit when voluntary`() = runTest {
        val viewModel = viewModel(forced = false)
        viewModel.onPasswordChange("newpassword123")
        viewModel.onPasswordConfirmationChange("newpassword123")

        assertFalse(viewModel.state.value.canSubmit)
        viewModel.submit()

        coVerify(exactly = 0) { meRepository.changePassword(any(), any(), any()) }
    }

    @Test
    fun `success refreshes the current user and marks done`() = runTest {
        val viewModel = viewModel(forced = true)
        viewModel.onPasswordChange("newpassword123")
        viewModel.onPasswordConfirmationChange("newpassword123")
        coEvery { meRepository.changePassword(null, "newpassword123", "newpassword123") } returns
            ChangePasswordResult.Success

        viewModel.submit()

        assertTrue(viewModel.state.value.done)
        assertNull(viewModel.state.value.error)
        coVerify { currentUserStore.refresh() }
    }

    @Test
    fun `invalid current password surfaces INVALID_CURRENT_PASSWORD and does not mark done`() = runTest {
        val viewModel = viewModel(forced = false)
        viewModel.onCurrentPasswordChange("wrong")
        viewModel.onPasswordChange("newpassword123")
        viewModel.onPasswordConfirmationChange("newpassword123")
        coEvery { meRepository.changePassword("wrong", "newpassword123", "newpassword123") } returns
            ChangePasswordResult.InvalidCurrentPassword

        viewModel.submit()

        assertEquals(ChangePasswordError.INVALID_CURRENT_PASSWORD, viewModel.state.value.error)
        assertFalse(viewModel.state.value.done)
    }

    @Test
    fun `offline result surfaces OFFLINE`() = runTest {
        val viewModel = viewModel(forced = true)
        viewModel.onPasswordChange("newpassword123")
        viewModel.onPasswordConfirmationChange("newpassword123")
        coEvery { meRepository.changePassword(any(), any(), any()) } returns ChangePasswordResult.Offline

        viewModel.submit()

        assertEquals(ChangePasswordError.OFFLINE, viewModel.state.value.error)
    }
}
