package it.bailettitommaso.fairly.data.remote.dto

import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.model.Tag
import it.bailettitommaso.fairly.domain.model.User

fun MeDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    mustChangePassword = mustChangePassword,
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
