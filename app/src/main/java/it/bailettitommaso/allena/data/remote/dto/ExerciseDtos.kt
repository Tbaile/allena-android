package it.bailettitommaso.allena.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseListEnvelopeDto(
    val data: List<ExerciseDto>,
    val meta: PaginationMetaDto,
)

@Serializable
data class PaginationMetaDto(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
)

/** `GET /exercises/{id}` wraps the payload in a top-level `data` key (Laravel JsonResource). */
@Serializable
data class ExerciseEnvelopeDto(val data: ExerciseDto)

@Serializable
data class ExerciseDto(
    val id: Long,
    val name: String,
    val description: String,
    val category: CategoryDto? = null,
    val tags: List<TagDto> = emptyList(),
    @SerialName("video_url") val videoUrl: String? = null,
)

@Serializable
data class TagDto(
    val id: Long,
    val name: String,
    val slug: String,
)

@Serializable
data class CategoryListEnvelopeDto(val data: List<CategoryDto>)

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String,
    val slug: String,
)
