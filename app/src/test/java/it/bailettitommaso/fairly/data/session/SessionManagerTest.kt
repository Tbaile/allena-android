package it.bailettitommaso.fairly.data.session

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.data.local.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private val tokenStore = mockk<TokenStore>(relaxed = true)

    private fun manager(scope: CoroutineScope) = SessionManager(tokenStore, scope)

    @Test
    fun `401 before authentication is a no-op (silent boot)`() = runTest {
        val sessionManager = manager(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        sessionManager.onUnauthorized()

        assertFalse(sessionManager.sessionExpired.value)
        coVerify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `401 after authentication clears token and raises the flag`() = runTest {
        val sessionManager = manager(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        sessionManager.markAuthenticated()

        sessionManager.sessionExpired.test {
            assertFalse(awaitItem()) // initial

            sessionManager.onUnauthorized()

            assertTrue(awaitItem())
        }
        coVerify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `a second concurrent 401 is idempotent`() = runTest {
        val sessionManager = manager(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        sessionManager.markAuthenticated()

        sessionManager.onUnauthorized()
        sessionManager.onUnauthorized()

        assertTrue(sessionManager.sessionExpired.value)
        coVerify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `markLoggedOut closes the gate so later 401 does not flash`() = runTest {
        val sessionManager = manager(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        sessionManager.markAuthenticated()
        sessionManager.markLoggedOut()

        sessionManager.onUnauthorized()

        assertFalse(sessionManager.sessionExpired.value)
        coVerify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `consumeSessionExpired resets the flag`() = runTest {
        val sessionManager = manager(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        sessionManager.markAuthenticated()
        sessionManager.onUnauthorized()

        sessionManager.consumeSessionExpired()

        assertFalse(sessionManager.sessionExpired.value)
    }
}
