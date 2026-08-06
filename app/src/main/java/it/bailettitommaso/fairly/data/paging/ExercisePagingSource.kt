package it.bailettitommaso.fairly.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.model.Exercise
import retrofit2.HttpException
import java.io.IOException

class ExercisePagingSource(
    private val exerciseApi: ExerciseApi,
    private val search: String?,
    private val categorySlug: String?,
) : PagingSource<Int, Exercise>() {

    override fun getRefreshKey(state: PagingState<Int, Exercise>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Exercise> {
        val page = params.key ?: 1
        return try {
            val response = exerciseApi.list(search = search, categorySlug = categorySlug, page = page)
            LoadResult.Page(
                data = response.data.map { it.toDomain() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= response.meta.lastPage) null else page + 1,
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}
