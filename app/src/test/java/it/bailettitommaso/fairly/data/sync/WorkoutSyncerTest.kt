package it.bailettitommaso.fairly.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.domain.repository.WorkoutRepository
import it.bailettitommaso.fairly.util.ConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WorkoutSyncerTest {

    private val workoutRepository = mockk<WorkoutRepository>(relaxed = true)
    private val tokenStore = mockk<TokenStore>()
    private val connectivityObserver = mockk<ConnectivityObserver>()
    private val status = MutableSharedFlow<ConnectivityObserver.Status>(replay = 1)

    private fun syncer(scope: CoroutineScope): WorkoutSyncer {
        every { connectivityObserver.status } returns status
        return WorkoutSyncer(workoutRepository, tokenStore, connectivityObserver, scope)
    }

    @Test
    fun `a connection uploads pending sessions`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        syncer(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).start()

        status.emit(ConnectivityObserver.Status.AVAILABLE)
        advanceUntilIdle()

        coVerify(exactly = 1) { workoutRepository.syncPending() }
    }

    @Test
    fun `losing the connection does not trigger an upload`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        syncer(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).start()

        status.emit(ConnectivityObserver.Status.UNAVAILABLE)
        advanceUntilIdle()

        coVerify(exactly = 0) { workoutRepository.syncPending() }
    }

    @Test
    fun `the state present at startup counts as a connection`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        status.emit(ConnectivityObserver.Status.AVAILABLE)

        syncer(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).start()
        advanceUntilIdle()

        coVerify(exactly = 1) { workoutRepository.syncPending() }
    }

    @Test
    fun `nothing is uploaded while signed out`() = runTest {
        coEvery { tokenStore.currentToken() } returns null
        syncer(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).start()

        status.emit(ConnectivityObserver.Status.AVAILABLE)
        advanceUntilIdle()

        coVerify(exactly = 0) { workoutRepository.syncPending() }
    }

    @Test
    fun `reconnecting repeatedly uploads again each time`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        syncer(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).start()

        status.emit(ConnectivityObserver.Status.AVAILABLE)
        advanceUntilIdle()
        status.emit(ConnectivityObserver.Status.UNAVAILABLE)
        advanceUntilIdle()
        status.emit(ConnectivityObserver.Status.AVAILABLE)
        advanceUntilIdle()

        coVerify(exactly = 2) { workoutRepository.syncPending() }
    }
}
