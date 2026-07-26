package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.CategoryDto
import it.bailettitommaso.fairly.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseListEnvelopeDto
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import it.bailettitommaso.fairly.domain.repository.ExercisesResult
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
    fun `list success maps DTOs to domain`() = runTest {
        coEvery { exerciseApi.list(any(), any(), any()) } returns ExerciseListEnvelopeDto(
            data = listOf(
                ExerciseDto(
                    id = 1,
                    name = "Plank Hold",
                    description = "Hold a plank.",
                    category = CategoryDto(id = 2, name = "Free Body", slug = "free-body"),
                ),
            ),
        )

        val result = repository.list(search = null, categorySlug = null)

        assertTrue(result is ExercisesResult.Success)
        val exercise = (result as ExercisesResult.Success).exercises.single()
        assertEquals("Plank Hold", exercise.name)
        assertEquals("free-body", exercise.category?.slug)
    }

    @Test
    fun `blank search is sent as null`() = runTest {
        coEvery { exerciseApi.list(any(), any(), any()) } returns ExerciseListEnvelopeDto(emptyList())

        repository.list(search = "   ", categorySlug = null)

        coVerify { exerciseApi.list(search = null, categorySlug = null, perPage = any()) }
    }

    @Test
    fun `list network failure maps to Offline`() = runTest {
        coEvery { exerciseApi.list(any(), any(), any()) } throws IOException("no network")

        val result = repository.list(search = null, categorySlug = null)

        assertEquals(ExercisesResult.Offline, result)
    }

    @Test
    fun `list server error maps to Error`() = runTest {
        coEvery { exerciseApi.list(any(), any(), any()) } throws http(500)

        val result = repository.list(search = null, categorySlug = null)

        assertEquals(ExercisesResult.Error, result)
    }

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

    private fun http(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
