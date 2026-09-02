package it.bailettitommaso.allena.ui.exercises

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import it.bailettitommaso.allena.MainDispatcherRule
import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import it.bailettitommaso.allena.domain.repository.ExerciseRepository
import it.bailettitommaso.allena.domain.repository.ExerciseResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepository = mockk<ExerciseRepository>()

    private val exercise = Exercise(
        id = 7,
        name = "Barbell Back Squat",
        description = "Sit down, stand up.",
        category = Category(3, "Strength", "strength"),
        videoUrl = null,
        tags = listOf(Tag(1, "Legs", "legs")),
    )

    private fun viewModel(id: Long = 7L) =
        ExerciseDetailViewModel(SavedStateHandle(mapOf("id" to id)), exerciseRepository)

    @Test
    fun `loads the exercise for the id from the route`() = runTest {
        coEvery { exerciseRepository.get(7) } returns ExerciseResult.Success(exercise)

        val state = viewModel().state.value

        assertTrue(state is ExerciseDetailUiState.Success)
        assertEquals(exercise, (state as ExerciseDetailUiState.Success).exercise)
    }

    @Test
    fun `network failure surfaces Offline`() = runTest {
        coEvery { exerciseRepository.get(7) } returns ExerciseResult.Offline

        assertEquals(ExerciseDetailUiState.Offline, viewModel().state.value)
    }

    @Test
    fun `missing exercise surfaces NotFound`() = runTest {
        coEvery { exerciseRepository.get(7) } returns ExerciseResult.NotFound

        assertEquals(ExerciseDetailUiState.NotFound, viewModel().state.value)
    }

    @Test
    fun `retry reloads the exercise`() = runTest {
        coEvery { exerciseRepository.get(7) } returns ExerciseResult.Offline
        val viewModel = viewModel()

        coEvery { exerciseRepository.get(7) } returns ExerciseResult.Success(exercise)
        viewModel.retry()

        assertTrue(viewModel.state.value is ExerciseDetailUiState.Success)
        coVerify(exactly = 2) { exerciseRepository.get(7) }
    }

    @Test
    fun `toggleFavorite flips favorite state and persists it`() = runTest {
        coEvery { exerciseRepository.get(7) } returns ExerciseResult.Success(exercise)
        coEvery { exerciseRepository.toggleFavorite(any()) } just runs
        val viewModel = viewModel()

        viewModel.toggleFavorite()

        val state = viewModel.state.value
        assertTrue(state is ExerciseDetailUiState.Success)
        assertTrue((state as ExerciseDetailUiState.Success).exercise.isFavorite)
        coVerify { exerciseRepository.toggleFavorite(exercise) }
    }
}
