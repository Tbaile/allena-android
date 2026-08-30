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
import org.junit.Assert.assertTrue
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
    fun `a state is routed once, so an activity recreation does not re-navigate`() = runTest {
        coEvery { sessionRepository.bootstrap() } returns unreachable
        val viewModel = BootViewModel(sessionRepository, sessionManager)
        val authenticated = BootState.Authenticated(
            User(id = 1, name = "Fairly Customer", email = "customer@fairly.app", mustChangePassword = false),
        )

        assertTrue(viewModel.shouldRoute(authenticated))
        // The replay after a rotation carries the same state and must be ignored.
        assertFalse(viewModel.shouldRoute(authenticated))
        assertFalse(viewModel.shouldRoute(authenticated))
    }

    @Test
    fun `a genuine state change still routes`() = runTest {
        coEvery { sessionRepository.bootstrap() } returns unreachable
        val viewModel = BootViewModel(sessionRepository, sessionManager)

        assertTrue(viewModel.shouldRoute(BootState.Unauthenticated))
        assertFalse(viewModel.shouldRoute(BootState.Unauthenticated))
        // A session expiring after the user signed in has to reach the login screen.
        assertTrue(viewModel.shouldRoute(BootState.Loading))
        assertTrue(viewModel.shouldRoute(BootState.Unauthenticated))
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
