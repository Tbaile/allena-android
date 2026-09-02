package it.bailettitommaso.allena.data.repository

import it.bailettitommaso.allena.data.local.db.ExerciseDao
import it.bailettitommaso.allena.data.local.db.WorkoutPlanDao
import it.bailettitommaso.allena.data.local.db.WorkoutPlanEntity
import it.bailettitommaso.allena.data.local.db.WorkoutSessionDao
import it.bailettitommaso.allena.data.local.db.WorkoutSessionEntity
import it.bailettitommaso.allena.data.local.db.WorkoutSetLogEntity
import it.bailettitommaso.allena.data.local.db.toDomain as toDomainFromCache
import it.bailettitommaso.allena.data.local.db.toEntity
import it.bailettitommaso.allena.data.remote.api.WorkoutApi
import it.bailettitommaso.allena.data.remote.dto.StoreWorkoutSessionDto
import it.bailettitommaso.allena.data.remote.dto.StoreWorkoutSetDto
import it.bailettitommaso.allena.data.remote.dto.WorkoutSessionDto
import it.bailettitommaso.allena.data.remote.dto.toDomain
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem
import it.bailettitommaso.allena.domain.model.WorkoutSession
import it.bailettitommaso.allena.domain.repository.SessionUploadResult
import it.bailettitommaso.allena.domain.repository.SessionsResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlanResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlansResult
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutApi: WorkoutApi,
    private val workoutPlanDao: WorkoutPlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val exerciseDao: ExerciseDao,
) : WorkoutRepository {

    override suspend fun plans(): WorkoutPlansResult {
        return try {
            val plans = workoutApi.plans().data.map { it.toDomain() }
            plans.forEach { cachePlan(it) }
            WorkoutPlansResult.Success(plans)
        } catch (e: HttpException) {
            Timber.d("workout plans: server error %d", e.code())
            WorkoutPlansResult.Error
        } catch (e: IOException) {
            Timber.d(e, "workout plans: network error, reading cache")
            val cached = workoutPlanDao.getPlans().map { it.toDomainFromCache(itemsFor(it.id)) }
            if (cached.isEmpty()) WorkoutPlansResult.Offline else WorkoutPlansResult.Success(cached)
        }
    }

    override suspend fun plan(id: Long): WorkoutPlanResult {
        return try {
            val plan = workoutApi.plan(id).data.toDomain()
            cachePlan(plan)
            WorkoutPlanResult.Success(plan)
        } catch (e: HttpException) {
            Timber.d("workout plan %d: server error %d", id, e.code())
            if (e.code() == HTTP_NOT_FOUND) WorkoutPlanResult.NotFound else WorkoutPlanResult.Error
        } catch (e: IOException) {
            Timber.d(e, "workout plan %d: network error, checking cache", id)
            val cached = workoutPlanDao.getPlan(id) ?: return WorkoutPlanResult.Offline
            val items = itemsFor(id)
            if (items.isEmpty()) {
                WorkoutPlanResult.Offline
            } else {
                WorkoutPlanResult.Success(cached.toDomainFromCache(items))
            }
        }
    }

    override fun sessions(): Flow<List<WorkoutSession>> =
        workoutSessionDao.sessions().map { entities -> entities.map { it.toDomainFromCache() } }

    override suspend fun refreshSessions(): SessionsResult {
        return try {
            val sessions = workoutApi.sessions().data.map { it.toEntity() }
            workoutSessionDao.replaceSynced(sessions)
            SessionsResult.Success
        } catch (e: HttpException) {
            Timber.d("workout sessions: server error %d", e.code())
            SessionsResult.Error
        } catch (e: IOException) {
            Timber.d(e, "workout sessions: network error, serving cache")
            SessionsResult.Offline
        }
    }

    override suspend fun startSession(planId: Long, startedAt: Instant): Long {
        val planName = workoutPlanDao.getPlan(planId)?.name

        return workoutSessionDao.insertSession(
            WorkoutSessionEntity(
                remoteId = null,
                planId = planId,
                planName = planName,
                startedAtMillis = startedAt.toEpochMilli(),
                completedAtMillis = null,
                notes = null,
                setCount = 0,
                totalVolume = 0.0,
                isPending = true,
            ),
        )
    }

    override suspend fun logSet(
        sessionLocalId: Long,
        planItemId: Long,
        setNumber: Int,
        reps: Int?,
        weight: Double?,
        durationSeconds: Int?,
    ) {
        workoutSessionDao.insertSetLog(
            WorkoutSetLogEntity(
                sessionLocalId = sessionLocalId,
                planItemId = planItemId,
                setNumber = setNumber,
                reps = reps,
                weight = weight,
                durationSeconds = durationSeconds,
            ),
        )
    }

    override suspend fun finishSession(
        sessionLocalId: Long,
        completedAt: Instant,
        notes: String?,
    ): SessionUploadResult {
        val setLogs = workoutSessionDao.getSetLogs(sessionLocalId)
        workoutSessionDao.markFinished(
            localId = sessionLocalId,
            completedAtMillis = completedAt.toEpochMilli(),
            notes = notes,
            setCount = setLogs.size,
            totalVolume = setLogs.sumOf { (it.reps ?: 0) * (it.weight ?: 0.0) },
        )

        return upload(sessionLocalId)
    }

    override suspend fun syncPending(): Int {
        val pending = workoutSessionDao.getPending().filter { it.completedAtMillis != null }

        return pending.count { upload(it.localId) is SessionUploadResult.Success }
    }

    override suspend fun discardSession(sessionLocalId: Long) {
        workoutSessionDao.deleteSession(sessionLocalId)
    }

    /**
     * Sends one finished session in a single request. A network failure is not an error:
     * the row stays pending and [syncPending] retries it once connectivity returns.
     */
    private suspend fun upload(sessionLocalId: Long): SessionUploadResult {
        val session = workoutSessionDao.getSession(sessionLocalId) ?: return SessionUploadResult.Error
        val completedAt = session.completedAtMillis ?: return SessionUploadResult.Error
        val setLogs = workoutSessionDao.getSetLogs(sessionLocalId)

        if (setLogs.isEmpty()) {
            Timber.d("session %d: nothing logged, discarding", sessionLocalId)
            workoutSessionDao.deleteSession(sessionLocalId)
            return SessionUploadResult.Error
        }

        return try {
            val uploaded = workoutApi.logSession(
                planId = session.planId,
                session = StoreWorkoutSessionDto(
                    startedAt = Instant.ofEpochMilli(session.startedAtMillis).toString(),
                    completedAt = Instant.ofEpochMilli(completedAt).toString(),
                    notes = session.notes,
                    sets = setLogs.map {
                        StoreWorkoutSetDto(
                            planItemId = it.planItemId,
                            setNumber = it.setNumber,
                            reps = it.reps,
                            weight = it.weight,
                            durationSeconds = it.durationSeconds,
                        )
                    },
                ),
            ).data

            workoutSessionDao.markSynced(sessionLocalId, uploaded.id)
            SessionUploadResult.Success
        } catch (e: IOException) {
            Timber.d(e, "session %d: network error, staying pending", sessionLocalId)
            SessionUploadResult.Pending
        } catch (e: HttpException) {
            Timber.d("session %d: server rejected upload with %d", sessionLocalId, e.code())
            SessionUploadResult.Error
        }
    }

    /** Plan exercises are written to the shared exercise cache so the detail screen works offline. */
    private suspend fun cachePlan(plan: WorkoutPlan) {
        exerciseDao.upsertAll(plan.items.map { it.exercise.toEntity() })
        workoutPlanDao.replacePlan(plan.toEntity(), plan.items.map { it.toEntity(plan.id) })
    }

    private suspend fun itemsFor(planId: Long): List<WorkoutPlanItem> {
        val items = workoutPlanDao.getItems(planId)
        if (items.isEmpty()) return emptyList()

        val exercises = exerciseDao.getByIds(items.map { it.exerciseId })
            .associateBy { it.id }

        return items.mapNotNull { item ->
            exercises[item.exerciseId]?.let { item.toDomainFromCache(it.toDomainFromCache()) }
        }
    }

    private fun WorkoutSessionDto.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
        remoteId = id,
        planId = workoutPlanId,
        planName = planName,
        startedAtMillis = Instant.parse(startedAt).toEpochMilli(),
        completedAtMillis = completedAt?.let { Instant.parse(it).toEpochMilli() },
        notes = notes,
        setCount = setCount,
        totalVolume = totalVolume,
        isPending = false,
    )

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
