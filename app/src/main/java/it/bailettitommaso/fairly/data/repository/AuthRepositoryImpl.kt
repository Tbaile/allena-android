package it.bailettitommaso.fairly.data.repository

import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.data.remote.api.AuthApi
import it.bailettitommaso.fairly.data.remote.dto.LoginRequestDto
import it.bailettitommaso.fairly.data.remote.dto.toDomain
import it.bailettitommaso.fairly.domain.repository.AuthRepository
import it.bailettitommaso.fairly.domain.repository.LoginResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = authApi.login(LoginRequestDto(email = email, password = password))
            tokenStore.save(response.token)
            LoginResult.Success(response.user.toDomain())
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNPROCESSABLE_ENTITY) {
                LoginResult.InvalidCredentials
            } else {
                LoginResult.Error
            }
        } catch (e: IOException) {
            LoginResult.Offline
        }
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}
