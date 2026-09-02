package it.bailettitommaso.allena.data.local.db

import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    category = categoryId?.let { Category(id = it, name = categoryName.orEmpty(), slug = categorySlug.orEmpty()) },
    videoUrl = videoUrl,
    tags = TagsJsonConverter.fromJson(tagsJson),
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    description = description,
    categoryId = category?.id,
    categoryName = category?.name,
    categorySlug = category?.slug,
    tagsJson = TagsJsonConverter.toJson(tags),
    videoUrl = videoUrl,
)
