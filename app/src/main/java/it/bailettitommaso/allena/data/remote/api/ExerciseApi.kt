package it.bailettitommaso.allena.data.remote.api

import it.bailettitommaso.allena.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.allena.data.remote.dto.ExerciseEnvelopeDto
import it.bailettitommaso.allena.data.remote.dto.ExerciseListEnvelopeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApi {
    @GET("exercises")
    suspend fun list(
        @Query("filter[search]") search: String? = null,
        @Query("filter[category]") categorySlug: String? = null,
        @Query("page") page: Int,
    ): ExerciseListEnvelopeDto

    @GET("exercises/{id}")
    suspend fun get(@Path("id") id: Long): ExerciseEnvelopeDto

    @GET("categories")
    suspend fun categories(): CategoryListEnvelopeDto
}
