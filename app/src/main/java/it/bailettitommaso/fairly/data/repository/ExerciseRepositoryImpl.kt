package it.bailettitommaso.fairly.data.repository

import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import it.bailettitommaso.fairly.domain.repository.ExerciseRepository
import it.bailettitommaso.fairly.domain.repository.ExercisesResult
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseApi: ExerciseApi,
) : ExerciseRepository {
    override suspend fun list(search: String?, categorySlug: String?): ExercisesResult {
        return try {
            val response = exerciseApi.list(
                search = search?.takeIf { it.isNotBlank() },
                categorySlug = categorySlug,
                perPage = MAX_PER_PAGE,
            )
            ExercisesResult.Success(response.data.map { it.toDomain() })
        } catch (e: HttpException) {
            Timber.d("exercises: server error %d", e.code())
            ExercisesResult.Error
        } catch (e: IOException) {
            Timber.d(e, "exercises: network error, offline")
            ExercisesResult.Offline
        }
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
        const val MAX_PER_PAGE = 100
    }
}
