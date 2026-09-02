package it.bailettitommaso.allena.data.remote.api

import it.bailettitommaso.allena.data.remote.dto.LoginRequestDto
import it.bailettitommaso.allena.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("auth/logout")
    suspend fun logout()
}
