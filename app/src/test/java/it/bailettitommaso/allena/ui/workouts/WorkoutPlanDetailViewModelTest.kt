package it.bailettitommaso.allena.ui.workouts

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.domain.repository.WorkoutPlanResult
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutPlanDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>()

    private fun viewModel(id: Long = 1) =
        WorkoutPlanDetailViewModel(SavedStateHandle(mapOf("id" to id)), workoutRepository)

    @Test
    fun `loads the plan named by the route argument`() = runTest {
        coEvery { workoutRepository.plan(7) } returns WorkoutPlanResult.Success(previewPlan)

        val state = viewModel(id = 7).state.value

        assertTrue(state is WorkoutPlanDetailUiState.Success)
        assertEquals(4, (state as WorkoutPlanDetailUiState.Success).plan.items.size)
        coVerify { workoutRepository.plan(7) }
    }

    @Test
    fun `a missing plan maps to NotFound`() = runTest {
        coEvery { workoutRepository.plan(any()) } returns WorkoutPlanResult.NotFound

        assertEquals(WorkoutPlanDetailUiState.NotFound, viewModel().state.value)
    }

    @Test
    fun `offline maps to the offline state`() = runTest {
        coEvery { workoutRepository.plan(any()) } returns WorkoutPlanResult.Offline

        assertEquals(WorkoutPlanDetailUiState.Offline, viewModel().state.value)
    }
}
