package it.bailettitommaso.fairly.data.remote.api

import it.bailettitommaso.fairly.data.remote.dto.StoreWorkoutSessionDto
import it.bailettitommaso.fairly.data.remote.dto.WorkoutPlanEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.WorkoutPlanListEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.WorkoutSessionEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.WorkoutSessionListEnvelopeDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutApi {
    @GET("me/workout-plans")
    suspend fun plans(@Query("page") page: Int = 1): WorkoutPlanListEnvelopeDto

    @GET("me/workout-plans/{id}")
    suspend fun plan(@Path("id") id: Long): WorkoutPlanEnvelopeDto

    @POST("me/workout-plans/{id}/sessions")
    suspend fun logSession(
        @Path("id") planId: Long,
        @Body session: StoreWorkoutSessionDto,
    ): WorkoutSessionEnvelopeDto

    @GET("me/workout-sessions")
    suspend fun sessions(@Query("page") page: Int = 1): WorkoutSessionListEnvelopeDto
}
