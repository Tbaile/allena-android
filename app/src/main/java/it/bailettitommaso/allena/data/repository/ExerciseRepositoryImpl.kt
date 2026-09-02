package it.bailettitommaso.allena.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import it.bailettitommaso.allena.data.local.db.ExerciseDao
import it.bailettitommaso.allena.data.local.db.FavoriteExerciseDao
import it.bailettitommaso.allena.data.local.db.FavoriteExerciseEntity
import it.bailettitommaso.allena.data.local.db.toDomain as toDomainFromCache
import it.bailettitommaso.allena.data.local.db.toEntity
import it.bailettitommaso.allena.data.paging.ExercisePagingSource
import it.bailettitommaso.allena.data.remote.api.ExerciseApi
import it.bailettitommaso.allena.data.remote.dto.toDomain
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.repository.CategoriesResult
import it.bailettitommaso.allena.domain.repository.ExerciseRepository
import it.bailettitommaso.allena.domain.repository.ExerciseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseApi: ExerciseApi,
    private val exerciseDao: ExerciseDao,
    private val favoriteExerciseDao: FavoriteExerciseDao,
) : ExerciseRepository {
    override fun list(search: String?, categorySlug: String?): Flow<PagingData<Exercise>> {
        val trimmedSearch = search?.takeIf { it.isNotBlank() }
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { ExercisePagingSource(exerciseApi, exerciseDao, trimmedSearch, categorySlug) },
        ).flow
    }

    override suspend fun get(id: Long): ExerciseResult {
        return try {
            val exercise = exerciseApi.get(id).data.toDomain()
            ExerciseResult.Success(exercise.copy(isFavorite = favoriteExerciseDao.isFavoriteNow(id)))
        } catch (e: HttpException) {
            Timber.d("exercise %d: server error %d", id, e.code())
            if (e.code() == HTTP_NOT_FOUND) ExerciseResult.NotFound else ExerciseResult.Error
        } catch (e: IOException) {
            Timber.d(e, "exercise %d: network error, checking cache", id)
            exerciseDao.getById(id)?.let {
                ExerciseResult.Success(it.toDomainFromCache().copy(isFavorite = favoriteExerciseDao.isFavoriteNow(id)))
            } ?: ExerciseResult.Offline
        }
    }

    override fun favorites(): Flow<List<Exercise>> =
        favoriteExerciseDao.favorites().map { entities ->
            entities.map { it.toDomainFromCache().copy(isFavorite = true) }
        }

    override suspend fun toggleFavorite(exercise: Exercise) {
        if (favoriteExerciseDao.isFavoriteNow(exercise.id)) {
            favoriteExerciseDao.remove(FavoriteExerciseEntity(exercise.id))
        } else {
            exerciseDao.upsertAll(listOf(exercise.toEntity()))
            favoriteExerciseDao.add(FavoriteExerciseEntity(exercise.id))
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
        const val PAGE_SIZE = 20
        const val HTTP_NOT_FOUND = 404
    }
}
