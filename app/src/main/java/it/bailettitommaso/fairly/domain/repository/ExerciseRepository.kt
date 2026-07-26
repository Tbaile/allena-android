package it.bailettitommaso.fairly.domain.repository

import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise

sealed interface ExercisesResult {
    data class Success(val exercises: List<Exercise>) : ExercisesResult
    data object Offline : ExercisesResult
    data object Error : ExercisesResult
}

sealed interface CategoriesResult {
    data class Success(val categories: List<Category>) : CategoriesResult
    data object Offline : CategoriesResult
    data object Error : CategoriesResult
}

interface ExerciseRepository {
    suspend fun list(search: String?, categorySlug: String?): ExercisesResult
    suspend fun categories(): CategoriesResult
}
