package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.CategoryDto
import it.bailettitommaso.fairly.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.TagDto
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import it.bailettitommaso.fairly.domain.repository.ExerciseResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
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

    @Test
    fun `get success maps DTO to domain including tags`() = runTest {
        coEvery { exerciseApi.get(7) } returns ExerciseEnvelopeDto(
            data = ExerciseDto(
                id = 7,
                name = "Barbell Back Squat",
                description = "Sit down, stand up.",
                category = CategoryDto(id = 3, name = "Strength", slug = "strength"),
                tags = listOf(TagDto(id = 1, name = "Legs", slug = "legs")),
            ),
        )

        val result = repository.get(7)

        assertTrue(result is ExerciseResult.Success)
        val exercise = (result as ExerciseResult.Success).exercise
        assertEquals("Barbell Back Squat", exercise.name)
        assertEquals("strength", exercise.category?.slug)
        assertEquals(listOf("legs"), exercise.tags.map { it.slug })
    }

    @Test
    fun `get 404 maps to NotFound`() = runTest {
        coEvery { exerciseApi.get(7) } throws httpException(404)

        val result = repository.get(7)

        assertEquals(ExerciseResult.NotFound, result)
    }

    @Test
    fun `get server failure maps to Error`() = runTest {
        coEvery { exerciseApi.get(7) } throws httpException(500)

        val result = repository.get(7)

        assertEquals(ExerciseResult.Error, result)
    }

    @Test
    fun `get network failure maps to Offline`() = runTest {
        coEvery { exerciseApi.get(7) } throws IOException("no network")

        val result = repository.get(7)

        assertEquals(ExerciseResult.Offline, result)
    }

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
