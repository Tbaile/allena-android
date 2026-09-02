package it.bailettitommaso.allena.data.local.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var database: AllenaDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AllenaDatabase::class.java).build()
        dao = database.exerciseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private val squat = Exercise(
        id = 1,
        name = "Barbell Back Squat",
        description = "Sit down, stand up.",
        category = Category(id = 3, name = "Strength", slug = "strength"),
        videoUrl = "https://example.com/squat.mp4",
        tags = listOf(Tag(id = 1, name = "Legs", slug = "legs")),
    )

    @Test
    fun upsertAllThenGetAllRoundTripsExerciseIncludingTags() = runTest {
        dao.upsertAll(listOf(squat.toEntity()))

        val cached = dao.getAll().first().single().toDomain()

        assertEquals(squat, cached)
    }

    @Test
    fun getByIdReturnsCachedExercise() = runTest {
        dao.upsertAll(listOf(squat.toEntity()))

        val cached = dao.getById(1)?.toDomain()

        assertEquals(squat, cached)
    }

    @Test
    fun getByIdReturnsNullWhenNotCached() = runTest {
        val cached = dao.getById(99)

        assertNull(cached)
    }

    @Test
    fun searchFiltersByNameAndCategorySlug() = runTest {
        dao.upsertAll(
            listOf(
                squat.toEntity(),
                squat.copy(id = 2, name = "Push-up", category = null).toEntity(),
            ),
        )

        val bySearch = dao.search(search = "squat", categorySlug = null)
        val byCategory = dao.search(search = null, categorySlug = "strength")

        assertEquals(listOf(1L), bySearch.map { it.id })
        assertEquals(listOf(1L), byCategory.map { it.id })
    }
}
