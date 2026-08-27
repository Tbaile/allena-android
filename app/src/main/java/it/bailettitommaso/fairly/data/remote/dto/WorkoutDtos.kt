package it.bailettitommaso.fairly.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlanListEnvelopeDto(
    val data: List<WorkoutPlanDto>,
    val meta: PaginationMetaDto,
)

@Serializable
data class WorkoutPlanEnvelopeDto(val data: WorkoutPlanDto)

@Serializable
data class WorkoutPlanDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val items: List<WorkoutPlanItemDto> = emptyList(),
)

@Serializable
data class WorkoutPlanItemDto(
    val id: Long,
    val position: Int,
    val sets: Int,
    val reps: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("rest_seconds") val restSeconds: Int,
    @SerialName("target_weight") val targetWeight: Double? = null,
    val notes: String? = null,
    val exercise: ExerciseDto,
)

@Serializable
data class WorkoutSessionListEnvelopeDto(
    val data: List<WorkoutSessionDto>,
    val meta: PaginationMetaDto,
)

@Serializable
data class WorkoutSessionEnvelopeDto(val data: WorkoutSessionDto)

@Serializable
data class WorkoutSessionDto(
    val id: Long,
    @SerialName("workout_plan_id") val workoutPlanId: Long,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val notes: String? = null,
    @SerialName("set_count") val setCount: Int = 0,
    @SerialName("total_volume") val totalVolume: Double = 0.0,
)

/** Body of `POST me/workout-plans/{id}/sessions`: one finished workout, uploaded in a single request. */
@Serializable
data class StoreWorkoutSessionDto(
    @SerialName("started_at") val startedAt: String,
    @SerialName("completed_at") val completedAt: String,
    val notes: String? = null,
    val sets: List<StoreWorkoutSetDto>,
)

@Serializable
data class StoreWorkoutSetDto(
    @SerialName("plan_item_id") val planItemId: Long,
    @SerialName("set_number") val setNumber: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
)
