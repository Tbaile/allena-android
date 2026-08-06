package it.bailettitommaso.fairly.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.ExerciseApi
import it.bailettitommaso.fairly.data.remote.dto.ExerciseDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.PaginationMetaDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ExercisePagingSourceTest {

    private val exerciseApi = mockk<ExerciseApi>()
    private val pagingSource = ExercisePagingSource(exerciseApi, search = null, categorySlug = null)
    private val testPager = TestPager(PagingConfig(pageSize = 20), pagingSource)

    private fun envelope(page: Int, lastPage: Int, itemCount: Int) = ExerciseListEnvelopeDto(
        data = (1..itemCount).map {
            ExerciseDto(id = it.toLong(), name = "Exercise $it", description = "")
        },
        meta = PaginationMetaDto(currentPage = page, lastPage = lastPage),
    )

    @Test
    fun `first page maps DTOs and computes next key from last_page`() = runTest {
        coEvery { exerciseApi.list(search = null, categorySlug = null, page = 1) } returns
            envelope(page = 1, lastPage = 2, itemCount = 20)

        val result = testPager.refresh() as PagingSource.LoadResult.Page

        assertEquals(20, result.data.size)
        assertEquals(2, result.nextKey)
        assertNull(result.prevKey)
    }

    @Test
    fun `last page has null next key`() = runTest {
        coEvery { exerciseApi.list(search = null, categorySlug = null, page = 1) } returns
            envelope(page = 1, lastPage = 1, itemCount = 5)

        val result = testPager.refresh() as PagingSource.LoadResult.Page

        assertNull(result.nextKey)
    }

    @Test
    fun `IOException maps to LoadResult Error`() = runTest {
        coEvery { exerciseApi.list(search = null, categorySlug = null, page = 1) } throws IOException("offline")

        val result = testPager.refresh()

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `HttpException maps to LoadResult Error`() = runTest {
        val error = HttpException(Response.error<Any>(500, "".toResponseBody("application/json".toMediaType())))
        coEvery { exerciseApi.list(search = null, categorySlug = null, page = 1) } throws error

        val result = testPager.refresh()

        assertTrue(result is PagingSource.LoadResult.Error)
    }
}
