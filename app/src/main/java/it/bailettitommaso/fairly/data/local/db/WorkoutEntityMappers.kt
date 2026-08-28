package it.bailettitommaso.fairly.data.local.db

import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.model.WorkoutPlanItem
import it.bailettitommaso.fairly.domain.model.WorkoutSession
import it.bailettitommaso.fairly.domain.model.WorkoutSetLog
import java.time.Instant

fun WorkoutPlanEntity.toDomain(items: List<WorkoutPlanItem>): WorkoutPlan = WorkoutPlan(
    id = id,
    name = name,
    description = description,
    isActive = isActive,
    items = items,
)

fun WorkoutPlan.toEntity(): WorkoutPlanEntity = WorkoutPlanEntity(
    id = id,
    name = name,
    description = description,
    isActive = isActive,
)

/**
 * The prescribed exercise comes from the shared exercise cache rather than the item row,
 * so an item whose exercise is missing locally cannot be rebuilt.
 */
fun WorkoutPlanItemEntity.toDomain(exercise: Exercise): WorkoutPlanItem = WorkoutPlanItem(
    id = id,
    position = position,
    sets = sets,
    reps = reps,
    durationSeconds = durationSeconds,
    restSeconds = restSeconds,
    targetWeight = targetWeight,
    notes = notes,
    exercise = exercise,
)

fun WorkoutPlanItem.toEntity(planId: Long): WorkoutPlanItemEntity = WorkoutPlanItemEntity(
    id = id,
    planId = planId,
    exerciseId = exercise.id,
    position = position,
    sets = sets,
    reps = reps,
    durationSeconds = durationSeconds,
    restSeconds = restSeconds,
    targetWeight = targetWeight,
    notes = notes,
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    localId = localId,
    remoteId = remoteId,
    planId = planId,
    planName = planName,
    startedAt = Instant.ofEpochMilli(startedAtMillis),
    completedAt = completedAtMillis?.let(Instant::ofEpochMilli),
    notes = notes,
    setCount = setCount,
    totalVolume = totalVolume,
    isPending = isPending,
)

fun WorkoutSetLogEntity.toDomain(): WorkoutSetLog = WorkoutSetLog(
    id = id,
    planItemId = planItemId,
    setNumber = setNumber,
    reps = reps,
    weight = weight,
    durationSeconds = durationSeconds,
)
