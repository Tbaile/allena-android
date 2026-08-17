package it.bailettitommaso.fairly.data.remote.api

import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.UpdateMeRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface MeApi {
    @GET("me")
    suspend fun me(): MeEnvelopeDto

    @PUT("me")
    suspend fun update(@Body body: UpdateMeRequestDto): MeEnvelopeDto
}
