package it.bailettitommaso.allena.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import it.bailettitommaso.allena.data.local.db.ExerciseDao
import it.bailettitommaso.allena.data.local.db.ExerciseEntity
import it.bailettitommaso.allena.data.local.db.WorkoutPlanDao
import it.bailettitommaso.allena.data.local.db.WorkoutPlanEntity
import it.bailettitommaso.allena.data.local.db.WorkoutPlanItemEntity
import it.bailettitommaso.allena.data.local.db.WorkoutSessionDao
import it.bailettitommaso.allena.data.local.db.WorkoutSessionEntity
import it.bailettitommaso.allena.data.local.db.WorkoutSetLogEntity
import it.bailettitommaso.allena.data.remote.api.WorkoutApi
import it.bailettitommaso.allena.data.remote.dto.CategoryDto
import it.bailettitommaso.allena.data.remote.dto.ExerciseDto
import it.bailettitommaso.allena.data.remote.dto.PaginationMetaDto
import it.bailettitommaso.allena.data.remote.dto.StoreWorkoutSessionDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutPlanDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutPlanEnvelopeDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutPlanItemDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutPlanListEnvelopeDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutSessionDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutSessionEnvelopeDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutSessionListEnvelopeDto
import it.bailettitommaso.allena.domain.repository.SessionUploadResult
import it.bailettitommaso.allena.domain.repository.SessionsResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlanResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlansResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant

class WorkoutRepositoryImplTest {

    private val workoutApi = mockk<WorkoutApi>()
    private val workoutPlanDao = mockk<WorkoutPlanDao>(relaxed = true)
    private val workoutSessionDao = mockk<WorkoutSessionDao>(relaxed = true)
    private val exerciseDao = mockk<ExerciseDao>(relaxed = true)
    private val repository = WorkoutRepositoryImpl(workoutApi, workoutPlanDao, workoutSessionDao, exerciseDao)

    private val exerciseDto = ExerciseDto(
        id = 5,
        name = "Barbell Back Squat",
        description = "Squat with a barbell.",
        category = CategoryDto(id = 1, name = "Strength", slug = "strength"),
    )

    private val planDto = WorkoutPlanDto(
        id = 3,
        name = "Full Body A",
        description = "Demo plan.",
        isActive = true,
        items = listOf(
            WorkoutPlanItemDto(
                id = 11,
                position = 1,
                sets = 4,
                reps = 8,
                restSeconds = 120,
                targetWeight = 60.0,
                exercise = exerciseDto,
            ),
        ),
    )

    private fun httpError(code: Int) =
        HttpException(Response.error<Unit>(code, "".toResponseBody("application/json".toMediaType())))

    @Test
    fun `plans success maps DTOs and caches them`() = runTest {
        coEvery { workoutApi.plans(any()) } returns WorkoutPlanListEnvelopeDto(
            data = listOf(planDto),
            meta = PaginationMetaDto(currentPage = 1, lastPage = 1),
        )

        val result = repository.plans()

        assertTrue(result is WorkoutPlansResult.Success)
        val plan = (result as WorkoutPlansResult.Success).plans.single()
        assertEquals("Full Body A", plan.name)
        assertEquals("Barbell Back Squat", plan.items.single().exercise.name)
        coVerify { workoutPlanDao.replacePlan(any(), any()) }
        coVerify { exerciseDao.upsertAll(any()) }
    }

    @Test
    fun `plans network failure falls back to the cache`() = runTest {
        coEvery { workoutApi.plans(any()) } throws IOException("no network")
        coEvery { workoutPlanDao.getPlans() } returns listOf(
            WorkoutPlanEntity(id = 3, name = "Full Body A", description = null, isActive = true),
        )
        coEvery { workoutPlanDao.getItems(3) } returns listOf(
            WorkoutPlanItemEntity(
                id = 11, planId = 3, exerciseId = 5, position = 1, sets = 4, reps = 8,
                durationSeconds = null, restSeconds = 120, targetWeight = 60.0, notes = null,
            ),
        )
        coEvery { exerciseDao.getByIds(listOf(5)) } returns listOf(
            ExerciseEntity(
                id = 5, name = "Barbell Back Squat", description = "Squat.", categoryId = 1,
                categoryName = "Strength", categorySlug = "strength", tagsJson = "[]", videoUrl = null,
            ),
        )

        val result = repository.plans()

        assertTrue(result is WorkoutPlansResult.Success)
        assertEquals(60.0, (result as WorkoutPlansResult.Success).plans.single().items.single().targetWeight)
    }

    @Test
    fun `plans network failure with an empty cache reports offline`() = runTest {
        coEvery { workoutApi.plans(any()) } throws IOException("no network")
        coEvery { workoutPlanDao.getPlans() } returns emptyList()

        assertEquals(WorkoutPlansResult.Offline, repository.plans())
    }

    @Test
    fun `plan 404 maps to NotFound`() = runTest {
        coEvery { workoutApi.plan(3) } throws httpError(404)

        assertEquals(WorkoutPlanResult.NotFound, repository.plan(3))
    }

    @Test
    fun `plan 500 maps to Error`() = runTest {
        coEvery { workoutApi.plan(3) } throws httpError(500)

        assertEquals(WorkoutPlanResult.Error, repository.plan(3))
    }

    @Test
    fun `plan success returns items in prescribed order`() = runTest {
        val second = planDto.items.single().copy(id = 12, position = 2, sets = 3, reps = 10)
        coEvery { workoutApi.plan(3) } returns WorkoutPlanEnvelopeDto(
            data = planDto.copy(items = listOf(second, planDto.items.single())),
        )

        val result = repository.plan(3)

        assertTrue(result is WorkoutPlanResult.Success)
        assertEquals(listOf(1, 2), (result as WorkoutPlanResult.Success).plan.items.map { it.position })
    }

    @Test
    fun `finishing a session uploads every logged set`() = runTest {
        coEvery { workoutSessionDao.getSetLogs(1) } returns listOf(
            WorkoutSetLogEntity(id = 1, sessionLocalId = 1, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
            WorkoutSetLogEntity(id = 2, sessionLocalId = 1, planItemId = 11, setNumber = 2, reps = 6, weight = 65.0, durationSeconds = null),
        )
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession()
        coEvery { workoutApi.logSession(any(), any()) } returns WorkoutSessionEnvelopeDto(
            data = sessionDto(id = 77),
        )
        val body = slot<StoreWorkoutSessionDto>()

        val result = repository.finishSession(1, Instant.parse("2026-08-27T18:45:00Z"), "Felt strong.")

        assertEquals(SessionUploadResult.Success, result)
        coVerify { workoutApi.logSession(3, capture(body)) }
        assertEquals(2, body.captured.sets.size)
        assertEquals(11, body.captured.sets.first().planItemId)
        assertEquals("2026-08-27T18:45:00Z", body.captured.completedAt)
        coVerify { workoutSessionDao.markSynced(1, 77) }
    }

    @Test
    fun `finishing a session records set count and volume`() = runTest {
        coEvery { workoutSessionDao.getSetLogs(1) } returns listOf(
            WorkoutSetLogEntity(id = 1, sessionLocalId = 1, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
            WorkoutSetLogEntity(id = 2, sessionLocalId = 1, planItemId = 12, setNumber = 1, reps = null, weight = null, durationSeconds = 45),
        )
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession()
        coEvery { workoutApi.logSession(any(), any()) } returns WorkoutSessionEnvelopeDto(data = sessionDto(id = 77))

        repository.finishSession(1, Instant.parse("2026-08-27T18:45:00Z"), null)

        // The timed set carries no reps or weight, so it contributes nothing to volume.
        coVerify { workoutSessionDao.markFinished(1, any(), null, 2, 480.0) }
    }

    @Test
    fun `a session that cannot be uploaded stays pending`() = runTest {
        coEvery { workoutSessionDao.getSetLogs(1) } returns listOf(
            WorkoutSetLogEntity(id = 1, sessionLocalId = 1, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
        )
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession()
        coEvery { workoutApi.logSession(any(), any()) } throws IOException("no network")

        val result = repository.finishSession(1, Instant.parse("2026-08-27T18:45:00Z"), null)

        assertEquals(SessionUploadResult.Pending, result)
        coVerify(exactly = 0) { workoutSessionDao.markSynced(any(), any()) }
        coVerify(exactly = 0) { workoutSessionDao.deleteSession(any()) }
    }

    @Test
    fun `a session the server rejects is an error and is not retried silently`() = runTest {
        coEvery { workoutSessionDao.getSetLogs(1) } returns listOf(
            WorkoutSetLogEntity(id = 1, sessionLocalId = 1, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
        )
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession()
        coEvery { workoutApi.logSession(any(), any()) } throws httpError(422)

        assertEquals(SessionUploadResult.Error, repository.finishSession(1, Instant.now(), null))
    }

    @Test
    fun `a session with no logged sets is discarded rather than uploaded`() = runTest {
        coEvery { workoutSessionDao.getSetLogs(1) } returns emptyList()
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession()

        val result = repository.finishSession(1, Instant.parse("2026-08-27T18:45:00Z"), null)

        assertEquals(SessionUploadResult.Error, result)
        coVerify { workoutSessionDao.deleteSession(1) }
        coVerify(exactly = 0) { workoutApi.logSession(any(), any()) }
    }

    @Test
    fun `syncPending uploads finished sessions and counts the successes`() = runTest {
        coEvery { workoutSessionDao.getPending() } returns listOf(
            pendingSession(localId = 1),
            pendingSession(localId = 2),
            // still in progress: no completedAt, so it must not be uploaded
            pendingSession(localId = 3).copy(completedAtMillis = null),
        )
        coEvery { workoutSessionDao.getSession(1) } returns pendingSession(localId = 1)
        coEvery { workoutSessionDao.getSession(2) } returns pendingSession(localId = 2)
        coEvery { workoutSessionDao.getSetLogs(any()) } returns listOf(
            WorkoutSetLogEntity(id = 1, sessionLocalId = 1, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
        )
        coEvery { workoutApi.logSession(any(), any()) } returns WorkoutSessionEnvelopeDto(data = sessionDto(id = 77)) andThenThrows IOException("no network")

        assertEquals(1, repository.syncPending())
        coVerify(exactly = 0) { workoutSessionDao.getSession(3) }
    }

    @Test
    fun `refreshing history replaces synced rows and keeps pending ones`() = runTest {
        coEvery { workoutApi.sessions(any()) } returns WorkoutSessionListEnvelopeDto(
            data = listOf(sessionDto(id = 77)),
            meta = PaginationMetaDto(currentPage = 1, lastPage = 1),
        )
        val stored = slot<List<WorkoutSessionEntity>>()

        assertEquals(SessionsResult.Success, repository.refreshSessions())

        coVerify { workoutSessionDao.replaceSynced(capture(stored)) }
        val entity = stored.captured.single()
        assertEquals(77L, entity.remoteId)
        assertEquals(false, entity.isPending)
        assertEquals(1350.0, entity.totalVolume, 0.0)
        assertEquals(Instant.parse("2026-08-27T18:00:00Z").toEpochMilli(), entity.startedAtMillis)
    }

    @Test
    fun `refreshing history offline leaves the cache alone`() = runTest {
        coEvery { workoutApi.sessions(any()) } throws IOException("no network")

        assertEquals(SessionsResult.Offline, repository.refreshSessions())
        coVerify(exactly = 0) { workoutSessionDao.replaceSynced(any()) }
    }

    @Test
    fun `starting a session opens a pending row named after its plan`() = runTest {
        coEvery { workoutPlanDao.getPlan(3) } returns
            WorkoutPlanEntity(id = 3, name = "Full Body A", description = null, isActive = true)
        coEvery { workoutSessionDao.insertSession(any()) } returns 9
        val row = slot<WorkoutSessionEntity>()

        val localId = repository.startSession(3, Instant.parse("2026-08-27T18:00:00Z"))

        assertEquals(9L, localId)
        coVerify { workoutSessionDao.insertSession(capture(row)) }
        assertEquals("Full Body A", row.captured.planName)
        assertTrue(row.captured.isPending)
        assertNull(row.captured.completedAtMillis)
    }

    private fun pendingSession(localId: Long = 1) = WorkoutSessionEntity(
        localId = localId,
        remoteId = null,
        planId = 3,
        planName = "Full Body A",
        startedAtMillis = Instant.parse("2026-08-27T18:00:00Z").toEpochMilli(),
        completedAtMillis = Instant.parse("2026-08-27T18:45:00Z").toEpochMilli(),
        notes = null,
        setCount = 0,
        totalVolume = 0.0,
        isPending = true,
    )

    private fun sessionDto(id: Long) = WorkoutSessionDto(
        id = id,
        workoutPlanId = 3,
        planName = "Full Body A",
        startedAt = "2026-08-27T18:00:00.000000Z",
        completedAt = "2026-08-27T18:45:00.000000Z",
        setCount = 4,
        totalVolume = 1350.0,
    )
}
