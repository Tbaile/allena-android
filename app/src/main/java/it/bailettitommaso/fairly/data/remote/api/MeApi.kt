package it.bailettitommaso.fairly.data.remote.api

import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.fairly.data.remote.dto.UpdateMeRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface MeApi {
    @GET("me")
    suspend fun me(): MeEnvelopeDto

    @PUT("me")
    suspend fun update(@Body body: UpdateMeRequestDto): MeEnvelopeDto

    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(@Part photo: MultipartBody.Part): MeEnvelopeDto

    @DELETE("me/avatar")
    suspend fun deleteAvatar(): MeEnvelopeDto
}
