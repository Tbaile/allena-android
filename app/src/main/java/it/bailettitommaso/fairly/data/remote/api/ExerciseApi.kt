package it.bailettitommaso.fairly.data.remote.api

import it.bailettitommaso.fairly.data.remote.dto.CategoryListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.ExerciseListEnvelopeDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExerciseApi {
    @GET("exercises")
    suspend fun list(
        @Query("search") search: String? = null,
        @Query("category") categorySlug: String? = null,
        @Query("per_page") perPage: Int? = null,
    ): ExerciseListEnvelopeDto

    @GET("categories")
    suspend fun categories(): CategoryListEnvelopeDto
}
