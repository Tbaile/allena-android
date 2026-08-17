package it.bailettitommaso.fairly.ui.boot

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.bailettitommaso.fairly.MainDispatcherRule
import it.bailettitommaso.fairly.data.session.SessionManager
import it.bailettitommaso.fairly.domain.model.User
import it.bailettitommaso.fairly.domain.repository.SessionRepository
import it.bailettitommaso.fairly.domain.repository.SessionResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class BootViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = mockk<SessionRepository>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)

    private val unreachable = SessionResult.Unreachable(SessionResult.Unreachable.Cause.SERVER)

    init {
        every { sessionManager.sessionExpired } returns MutableStateFlow(false)
    }

    @Test
    fun `retry does not flip back to Loading`() = runTest {
        coEvery { sessionRepository.bootstrap() } returns unreachable
        val viewModel = BootViewModel(sessionRepository, sessionManager)

        viewModel.state.test {
            assertEquals(BootState.Unreachable(SessionResult.Unreachable.Cause.SERVER), awaitItem())

            viewModel.retry()

            // A Loading interstitial here is what used to re-trigger navigation and loop.
            expectNoEvents()
        }
    }

    @Test
    fun `retry while one is in flight is ignored`() = runTest {
        coEvery { sessionRepository.bootstrap() } returns unreachable
        val viewModel = BootViewModel(sessionRepository, sessionManager)

        val gate = CompletableDeferred<Unit>()
        coEvery { sessionRepository.bootstrap() } coAnswers {
            gate.await()
            unreachable
        }

        viewModel.retry()
        viewModel.retry()
        gate.complete(Unit)

        coVerify(exactly = 2) { sessionRepository.bootstrap() } // one boot + one retry
        assertFalse(viewModel.retrying.value)
    }

    @Test
    fun `successful boot marks the session authenticated`() = runTest {
        val user = User(id = 1, name = "Jane", email = "jane@example.com", mustChangePassword = false)
        coEvery { sessionRepository.bootstrap() } returns SessionResult.Authenticated(user)

        val viewModel = BootViewModel(sessionRepository, sessionManager)

        assertEquals(BootState.Authenticated(user), viewModel.state.value)
        coVerify { sessionManager.markAuthenticated() }
    }
}
