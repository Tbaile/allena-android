package it.bailettitommaso.fairly.data.repository

import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.UpdateMeRequestDto
import it.bailettitommaso.fairly.domain.repository.ChangePasswordResult
import it.bailettitommaso.fairly.domain.repository.MeRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class MeRepositoryImpl @Inject constructor(
    private val meApi: MeApi,
) : MeRepository {
    override suspend fun changePassword(
        currentPassword: String?,
        password: String,
        passwordConfirmation: String,
    ): ChangePasswordResult {
        return try {
            meApi.update(
                UpdateMeRequestDto(
                    currentPassword = currentPassword,
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                ),
            )
            Timber.d("changePassword: success")
            ChangePasswordResult.Success
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNPROCESSABLE_ENTITY) {
                Timber.d("changePassword: invalid current password")
                ChangePasswordResult.InvalidCurrentPassword
            } else {
                Timber.d("changePassword: server error %d", e.code())
                ChangePasswordResult.Error
            }
        } catch (e: IOException) {
            Timber.d(e, "changePassword: network error, offline")
            ChangePasswordResult.Offline
        }
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}
