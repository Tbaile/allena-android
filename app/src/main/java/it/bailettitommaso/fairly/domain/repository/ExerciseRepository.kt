package it.bailettitommaso.fairly.domain.repository

import androidx.paging.PagingData
import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

sealed interface CategoriesResult {
    data class Success(val categories: List<Category>) : CategoriesResult
    data object Offline : CategoriesResult
    data object Error : CategoriesResult
}

interface ExerciseRepository {
    fun list(search: String?, categorySlug: String?): Flow<PagingData<Exercise>>
    suspend fun categories(): CategoriesResult
}
