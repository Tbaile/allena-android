package it.bailettitommaso.fairly.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import it.bailettitommaso.fairly.data.paging.ExercisePagingSource
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import it.bailettitommaso.fairly.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseApi: ExerciseApi,
) : ExerciseRepository {
    override fun list(search: String?, categorySlug: String?): Flow<PagingData<Exercise>> {
        val trimmedSearch = search?.takeIf { it.isNotBlank() }
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { ExercisePagingSource(exerciseApi, trimmedSearch, categorySlug) },
        ).flow
    }

    override suspend fun categories(): CategoriesResult {
        return try {
            val response = exerciseApi.categories()
            CategoriesResult.Success(response.data.map { it.toDomain() })
        } catch (e: HttpException) {
            Timber.d("categories: server error %d", e.code())
            CategoriesResult.Error
        } catch (e: IOException) {
            Timber.d(e, "categories: network error, offline")
            CategoriesResult.Offline
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
