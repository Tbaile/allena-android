package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.data.local.db.ExerciseDao
import it.bailettitommaso.fairly.data.local.db.ExerciseEntity
import it.bailettitommaso.fairly.data.local.db.FavoriteExerciseDao
import it.bailettitommaso.fairly.data.local.db.FavoriteExerciseEntity
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.CategoryDto
import it.bailettitommaso.fairly.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.TagDto
import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
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
    private val exerciseDao = mockk<ExerciseDao>(relaxed = true)
    private val favoriteExerciseDao = mockk<FavoriteExerciseDao>(relaxed = true)
    private val repository = ExerciseRepositoryImpl(exerciseApi, exerciseDao, favoriteExerciseDao)

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
    fun `get network failure with no cache maps to Offline`() = runTest {
        coEvery { exerciseApi.get(7) } throws IOException("no network")
        coEvery { exerciseDao.getById(7) } returns null

        val result = repository.get(7)

        assertEquals(ExerciseResult.Offline, result)
    }

    @Test
    fun `get network failure with cache hit maps to Success from cache`() = runTest {
        coEvery { exerciseApi.get(7) } throws IOException("no network")
        coEvery { exerciseDao.getById(7) } returns ExerciseEntity(
            id = 7,
            name = "Cached Squat",
            description = "Sit down, stand up.",
            categoryId = null,
            categoryName = null,
            categorySlug = null,
            tagsJson = "[]",
            videoUrl = null,
        )

        val result = repository.get(7)

        assertTrue(result is ExerciseResult.Success)
        assertEquals("Cached Squat", (result as ExerciseResult.Success).exercise.name)
    }

    @Test
    fun `toggleFavorite caches the exercise and adds it when not yet favorited`() = runTest {
        val exercise = Exercise(
            id = 7,
            name = "Barbell Back Squat",
            description = "Sit down, stand up.",
            category = Category(3, "Strength", "strength"),
            videoUrl = null,
        )
        coEvery { favoriteExerciseDao.isFavoriteNow(7) } returns false

        repository.toggleFavorite(exercise)

        coVerify { exerciseDao.upsertAll(match { it.single().id == 7L }) }
        coVerify { favoriteExerciseDao.add(FavoriteExerciseEntity(7)) }
    }

    @Test
    fun `toggleFavorite removes it when already favorited`() = runTest {
        val exercise = Exercise(id = 7, name = "Barbell Back Squat", description = "", category = null, videoUrl = null)
        coEvery { favoriteExerciseDao.isFavoriteNow(7) } returns true

        repository.toggleFavorite(exercise)

        coVerify { favoriteExerciseDao.remove(FavoriteExerciseEntity(7)) }
        coVerify(exactly = 0) { exerciseDao.upsertAll(any()) }
    }

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
