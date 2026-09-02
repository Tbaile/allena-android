package it.bailettitommaso.allena.ui.workouts

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.domain.repository.SessionUploadResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlanResult
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkoutPlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>(relaxed = true)

    @Before
    fun setUp() {
        coEvery { workoutRepository.plan(any()) } returns WorkoutPlanResult.Success(previewPlan)
        coEvery { workoutRepository.startSession(any(), any()) } returns 42
        coEvery { workoutRepository.finishSession(any(), any(), any()) } returns SessionUploadResult.Success
    }

    private fun viewModel() =
        WorkoutPlayerViewModel(SavedStateHandle(mapOf("planId" to 1L)), workoutRepository)

    private fun running(state: WorkoutPlayerUiState) = state as WorkoutPlayerUiState.Running

    @Test
    fun `opening the player starts a session on the first exercise`() = runTest {
        val state = running(viewModel().state.value)

        assertEquals("Jump Squat", state.item.exercise.name)
        assertEquals(1, state.setNumber)
        assertEquals(4, state.itemCount)
        // Prefilled from what the plan prescribes.
        assertEquals("5", state.entry)
        assertEquals("10", state.weight)
        coVerify { workoutRepository.startSession(1, any()) }
    }

    @Test
    fun `completing a set logs it and moves to the next set of the same exercise`() = runTest {
        val viewModel = viewModel()
        viewModel.onEntryChange("6")
        viewModel.onWeightChange("12.5")

        viewModel.completeSet()

        coVerify {
            workoutRepository.logSet(
                sessionLocalId = 42, planItemId = previewPlan.items[0].id, setNumber = 1,
                reps = 6, weight = 12.5, durationSeconds = null,
            )
        }
        val state = running(viewModel.state.value)
        assertEquals(2, state.setNumber)
        assertEquals("Jump Squat", state.item.exercise.name)
        // The athlete's own numbers carry over within an exercise.
        assertEquals("6", state.entry)
        assertEquals("12.5", state.weight)
    }

    @Test
    fun `finishing an exercise moves on and re-prefills from the plan`() = runTest {
        val viewModel = viewModel()
        viewModel.onEntryChange("7")
        repeat(previewPlan.items[0].sets) {
            viewModel.completeSet()
            viewModel.skipRest()
        }

        val state = running(viewModel.state.value)
        assertEquals("Barbell Back Squat", state.item.exercise.name)
        assertEquals(1, state.setNumber)
        assertEquals("8", state.entry)
        assertEquals("60", state.weight)
    }

    @Test
    fun `a completed set starts the rest countdown, which ticks down and clears`() = runTest {
        val viewModel = viewModel()

        viewModel.completeSet()

        assertEquals(120, running(viewModel.state.value).restRemainingSeconds)
        // advanceTimeBy runs tasks scheduled strictly before the target, so step just past the tick.
        advanceTimeBy(3_100)
        assertEquals(117, running(viewModel.state.value).restRemainingSeconds)
        advanceTimeBy(120_000)
        assertNull(running(viewModel.state.value).restRemainingSeconds)
    }

    @Test
    fun `skipping rest clears the countdown immediately`() = runTest {
        val viewModel = viewModel()
        viewModel.completeSet()
        assertTrue(running(viewModel.state.value).isResting)

        viewModel.skipRest()

        assertFalse(running(viewModel.state.value).isResting)
        advanceTimeBy(5_000)
        assertNull(running(viewModel.state.value).restRemainingSeconds)
    }

    @Test
    fun `skipping a set logs nothing and moves on without resting`() = runTest {
        val viewModel = viewModel()

        viewModel.skipSet()

        coVerify(exactly = 0) { workoutRepository.logSet(any(), any(), any(), any(), any(), any()) }
        val state = running(viewModel.state.value)
        assertEquals(2, state.setNumber)
        assertEquals("Jump Squat", state.item.exercise.name)
        assertFalse(state.isResting)
    }

    @Test
    fun `skipping the last set of an exercise moves to the next one`() = runTest {
        val viewModel = viewModel()

        repeat(previewPlan.items[0].sets) { viewModel.skipSet() }

        val state = running(viewModel.state.value)
        assertEquals("Barbell Back Squat", state.item.exercise.name)
        assertEquals(1, state.setNumber)
    }

    @Test
    fun `skipped sets are absent from the finished totals`() = runTest {
        val viewModel = viewModel()
        viewModel.skipSet()
        viewModel.completeSet()
        viewModel.skipRest()

        viewModel.finishEarly()

        val state = viewModel.state.value as WorkoutPlayerUiState.Finished
        assertEquals(1, state.setCount)
    }

    @Test
    fun `skipping cancels a rest already running`() = runTest {
        val viewModel = viewModel()
        viewModel.completeSet()
        assertTrue(running(viewModel.state.value).isResting)

        viewModel.skipSet()

        assertFalse(running(viewModel.state.value).isResting)
        advanceTimeBy(5_000)
        assertNull(running(viewModel.state.value).restRemainingSeconds)
    }

    @Test
    fun `the last set cannot be skipped, since Finish already ends the workout`() = runTest {
        val single = previewPlan.copy(items = listOf(previewPlan.items[1].copy(sets = 1)))
        coEvery { workoutRepository.plan(any()) } returns WorkoutPlanResult.Success(single)
        val viewModel = viewModel()
        assertTrue(running(viewModel.state.value).isLastSet)

        viewModel.skipSet()

        assertTrue(viewModel.state.value is WorkoutPlayerUiState.Running)
    }

    @Test
    fun `a timed exercise logs a duration instead of reps`() = runTest {
        coEvery { workoutRepository.plan(any()) } returns
            WorkoutPlanResult.Success(previewPlan.copy(items = listOf(previewPlan.items[3])))
        val viewModel = viewModel()
        assertEquals("45", running(viewModel.state.value).entry)

        viewModel.completeSet()

        coVerify {
            workoutRepository.logSet(
                sessionLocalId = 42, planItemId = previewPlan.items[3].id, setNumber = 1,
                reps = null, weight = null, durationSeconds = 45,
            )
        }
    }

    @Test
    fun `the last set of the last exercise finishes the workout`() = runTest {
        val single = previewPlan.copy(items = listOf(previewPlan.items[1].copy(sets = 2)))
        coEvery { workoutRepository.plan(any()) } returns WorkoutPlanResult.Success(single)
        val viewModel = viewModel()

        viewModel.completeSet()
        viewModel.skipRest()
        assertTrue(running(viewModel.state.value).isLastSet)
        viewModel.completeSet()

        val state = viewModel.state.value
        assertTrue(state is WorkoutPlayerUiState.Finished)
        assertEquals(2, (state as WorkoutPlayerUiState.Finished).setCount)
        assertEquals(960.0, state.totalVolume, 0.0)
        assertFalse(state.pending)
        coVerify { workoutRepository.finishSession(42, any(), null) }
    }

    @Test
    fun `a workout that could not be uploaded is reported as pending`() = runTest {
        coEvery { workoutRepository.finishSession(any(), any(), any()) } returns SessionUploadResult.Pending
        val viewModel = viewModel()

        viewModel.finishEarly()

        assertTrue((viewModel.state.value as WorkoutPlayerUiState.Finished).pending)
    }

    @Test
    fun `finishing early keeps the sets logged so far`() = runTest {
        val viewModel = viewModel()
        viewModel.completeSet()
        viewModel.skipRest()

        viewModel.finishEarly()

        val state = viewModel.state.value as WorkoutPlayerUiState.Finished
        assertEquals(1, state.setCount)
        assertEquals(50.0, state.totalVolume, 0.0)
    }

    @Test
    fun `discarding throws the session away`() = runTest {
        val viewModel = viewModel()

        viewModel.discard()

        coVerify { workoutRepository.discardSession(42) }
        coVerify(exactly = 0) { workoutRepository.finishSession(any(), any(), any()) }
    }

    @Test
    fun `a plan with no exercises cannot be run`() = runTest {
        coEvery { workoutRepository.plan(any()) } returns
            WorkoutPlanResult.Success(previewPlan.copy(items = emptyList()))

        assertEquals(WorkoutPlayerUiState.NotFound, viewModel().state.value)
        coVerify(exactly = 0) { workoutRepository.startSession(any(), any()) }
    }

    @Test
    fun `entry rejects anything that is not a digit`() = runTest {
        val viewModel = viewModel()

        viewModel.onEntryChange("12a-3")
        assertEquals("123", running(viewModel.state.value).entry)

        viewModel.onWeightChange("6o0.5x")
        assertEquals("60.5", running(viewModel.state.value).weight)
    }
}
