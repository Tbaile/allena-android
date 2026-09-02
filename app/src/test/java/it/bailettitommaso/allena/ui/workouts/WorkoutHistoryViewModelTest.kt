package it.bailettitommaso.allena.ui.workouts

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.domain.model.WorkoutSession
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WorkoutHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>(relaxed = true)

    private fun session(startedAt: Instant, volume: Double, isPending: Boolean = false) = WorkoutSession(
        localId = 1,
        remoteId = null,
        planId = 1,
        planName = "Full Body A",
        startedAt = startedAt,
        completedAt = startedAt,
        notes = null,
        setCount = 4,
        totalVolume = volume,
        isPending = isPending,
    )

    @Test
    fun `history comes from the cache and drives the chart`() = runTest {
        val thisWeek = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { workoutRepository.sessions() } returns flowOf(listOf(session(thisWeek, 1200.0)))

        WorkoutHistoryViewModel(workoutRepository).state.test {
            val loaded = awaitItem()
            assertEquals(1, loaded.sessions.size)
            assertEquals(DEFAULT_WEEKS, loaded.chart.size)
            assertEquals(1200.0, loaded.chart.last().volume, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pending sessions are part of the history`() = runTest {
        val now = Instant.now()
        every { workoutRepository.sessions() } returns flowOf(listOf(session(now, 800.0, isPending = true)))

        WorkoutHistoryViewModel(workoutRepository).state.test {
            assertTrue(awaitItem().sessions.single().isPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `opening the screen refreshes from the server`() = runTest {
        every { workoutRepository.sessions() } returns flowOf(emptyList())

        WorkoutHistoryViewModel(workoutRepository)

        coVerify { workoutRepository.refreshSessions() }
    }
}
