package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.CategoryDto
import it.bailettitommaso.fairly.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ExerciseRepositoryImplTest {

    private val exerciseApi = mockk<ExerciseApi>()
    private val repository = ExerciseRepositoryImpl(exerciseApi)

    @Test
    fun `categories success maps DTOs to domain`() = runTest {
        coEvery { exerciseApi.categories() } returns CategoryListEnvelopeDto(
            data = listOf(CategoryDto(id = 1, name = "Yoga", slug = "yoga")),
        )

        val result = repository.categories()

        assertTrue(result is CategoriesResult.Success)
        assertEquals("yoga", (result as CategoriesResult.Success).categories.single().slug)
    }

    @Test
    fun `categories network failure maps to Offline`() = runTest {
        coEvery { exerciseApi.categories() } throws IOException("no network")

        val result = repository.categories()

        assertEquals(CategoriesResult.Offline, result)
    }
}
