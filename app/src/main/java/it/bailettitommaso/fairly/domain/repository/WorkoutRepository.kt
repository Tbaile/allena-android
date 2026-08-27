package it.bailettitommaso.fairly.domain.repository

import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant

sealed interface WorkoutPlansResult {
    data class Success(val plans: List<WorkoutPlan>) : WorkoutPlansResult
    data object Offline : WorkoutPlansResult
    data object Error : WorkoutPlansResult
}

sealed interface WorkoutPlanResult {
    data class Success(val plan: WorkoutPlan) : WorkoutPlanResult
    data object NotFound : WorkoutPlanResult
    data object Offline : WorkoutPlanResult
    data object Error : WorkoutPlanResult
}

sealed interface SessionsResult {
    data object Success : SessionsResult
    data object Offline : SessionsResult
    data object Error : SessionsResult
}

sealed interface SessionUploadResult {
    data object Success : SessionUploadResult

    /** Stored locally but not uploaded; it will be retried when connectivity returns. */
    data object Pending : SessionUploadResult
    data object Error : SessionUploadResult
}

interface WorkoutRepository {
    suspend fun plans(): WorkoutPlansResult

    suspend fun plan(id: Long): WorkoutPlanResult

    /** Session history, newest first, straight from the cache so pending workouts are included. */
    fun sessions(): Flow<List<WorkoutSession>>

    suspend fun refreshSessions(): SessionsResult

    /** Opens a local session row and returns its local id. */
    suspend fun startSession(planId: Long, startedAt: Instant): Long

    suspend fun logSet(
        sessionLocalId: Long,
        planItemId: Long,
        setNumber: Int,
        reps: Int?,
        weight: Double?,
        durationSeconds: Int?,
    )

    suspend fun finishSession(
        sessionLocalId: Long,
        completedAt: Instant,
        notes: String?,
    ): SessionUploadResult

    /** Uploads every session still flagged pending. Returns how many reached the server. */
    suspend fun syncPending(): Int

    suspend fun discardSession(sessionLocalId: Long)
}
