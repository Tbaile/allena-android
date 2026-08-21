package it.bailettitommaso.fairly.domain.model

data class Exercise(
    val id: Long,
    val name: String,
    val description: String,
    val category: Category?,
    val videoUrl: String?,
    val tags: List<Tag> = emptyList(),
    val isFavorite: Boolean = false,
)
