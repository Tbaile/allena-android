package it.bailettitommaso.allena.data.remote.dto

import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import it.bailettitommaso.allena.domain.model.User
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem

fun MeDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    mustChangePassword = mustChangePassword,
    avatarUrl = avatarUrl,
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    mustChangePassword = false,
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug,
)

fun TagDto.toDomain(): Tag = Tag(
    id = id,
    name = name,
    slug = slug,
)

fun ExerciseDto.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    category = category?.toDomain(),
    videoUrl = videoUrl,
    tags = tags.map { it.toDomain() },
)

fun WorkoutPlanItemDto.toDomain(): WorkoutPlanItem = WorkoutPlanItem(
    id = id,
    position = position,
    sets = sets,
    reps = reps,
    durationSeconds = durationSeconds,
    restSeconds = restSeconds,
    targetWeight = targetWeight,
    notes = notes,
    exercise = exercise.toDomain(),
)

fun WorkoutPlanDto.toDomain(): WorkoutPlan = WorkoutPlan(
    id = id,
    name = name,
    description = description,
    isActive = isActive,
    items = items.sortedBy { it.position }.map { it.toDomain() },
)
