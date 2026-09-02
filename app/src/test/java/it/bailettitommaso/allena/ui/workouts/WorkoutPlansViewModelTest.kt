package it.bailettitommaso.allena.ui.workouts

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.domain.repository.WorkoutPlansResult
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutPlansViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>()

    private val plan = WorkoutPlan(id = 1, name = "Full Body A", description = null, isActive = true)

    @Test
    fun `loads the assigned plans on creation`() = runTest {
        coEvery { workoutRepository.plans() } returns WorkoutPlansResult.Success(listOf(plan))

        val state = WorkoutPlansViewModel(workoutRepository).state.value

        assertTrue(state is WorkoutPlansUiState.Success)
        assertEquals("Full Body A", (state as WorkoutPlansUiState.Success).plans.single().name)
    }

    @Test
    fun `an empty plan list is a success, not an error`() = runTest {
        coEvery { workoutRepository.plans() } returns WorkoutPlansResult.Success(emptyList())

        assertEquals(WorkoutPlansUiState.Success(emptyList()), WorkoutPlansViewModel(workoutRepository).state.value)
    }

    @Test
    fun `offline maps to the offline state`() = runTest {
        coEvery { workoutRepository.plans() } returns WorkoutPlansResult.Offline

        assertEquals(WorkoutPlansUiState.Offline, WorkoutPlansViewModel(workoutRepository).state.value)
    }

    @Test
    fun `retry reloads after a failure`() = runTest {
        coEvery { workoutRepository.plans() } returns WorkoutPlansResult.Error andThen
            WorkoutPlansResult.Success(listOf(plan))
        val viewModel = WorkoutPlansViewModel(workoutRepository)
        assertEquals(WorkoutPlansUiState.Error, viewModel.state.value)

        viewModel.retry()

        assertTrue(viewModel.state.value is WorkoutPlansUiState.Success)
    }
}
