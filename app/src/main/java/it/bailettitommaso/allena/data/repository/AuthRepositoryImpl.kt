package it.bailettitommaso.allena.data.repository

import it.bailettitommaso.allena.data.local.TokenStore
import it.bailettitommaso.allena.data.remote.api.AuthApi
import it.bailettitommaso.allena.data.remote.dto.LoginRequestDto
import it.bailettitommaso.allena.data.remote.dto.toDomain
import it.bailettitommaso.allena.domain.repository.AuthRepository
import it.bailettitommaso.allena.domain.repository.LoginResult
import retrofit2.HttpException
import timber.log.Timber
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
            Timber.d("login: success")
            LoginResult.Success(response.user.toDomain())
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNPROCESSABLE_ENTITY) {
                Timber.d("login: invalid credentials")
                LoginResult.InvalidCredentials
            } else {
                Timber.d("login: server error %d", e.code())
                LoginResult.Error
            }
        } catch (e: IOException) {
            Timber.d(e, "login: network error, offline")
            LoginResult.Offline
        }
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            Timber.d(e, "logout: server call failed, clearing local session anyway")
        } finally {
            tokenStore.clear()
        }
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}
