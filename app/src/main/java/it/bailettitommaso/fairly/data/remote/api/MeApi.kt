package it.bailettitommaso.fairly.data.remote.api

import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import retrofit2.http.GET

interface MeApi {
    @GET("me")
    suspend fun me(): MeEnvelopeDto
}
