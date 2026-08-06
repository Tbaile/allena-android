package it.bailettitommaso.fairly.data.remote.dto

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

@Serializable
data class ExerciseDto(
    val id: Long,
    val name: String,
    val description: String,
    val category: CategoryDto? = null,
    @SerialName("video_url") val videoUrl: String? = null,
)

@Serializable
data class CategoryListEnvelopeDto(val data: List<CategoryDto>)

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String,
    val slug: String,
)
