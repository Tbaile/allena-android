package it.bailettitommaso.allena.data.repository

import it.bailettitommaso.allena.data.remote.api.MeApi
import it.bailettitommaso.allena.data.remote.dto.UpdateMeRequestDto
import it.bailettitommaso.allena.data.remote.dto.toDomain
import it.bailettitommaso.allena.domain.repository.AvatarResult
import it.bailettitommaso.allena.domain.repository.ChangePasswordResult
import it.bailettitommaso.allena.domain.repository.MeRepository
import it.bailettitommaso.allena.domain.repository.UpdateProfileResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
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

    override suspend fun updateName(name: String): UpdateProfileResult {
        return try {
            val user = meApi.update(UpdateMeRequestDto(name = name)).data.toDomain()
            Timber.d("updateName: success")
            UpdateProfileResult.Success(user)
        } catch (e: HttpException) {
            Timber.d("updateName: server error %d", e.code())
            UpdateProfileResult.Error
        } catch (e: IOException) {
            Timber.d(e, "updateName: network error, offline")
            UpdateProfileResult.Offline
        }
    }

    override suspend fun uploadAvatar(photo: File): AvatarResult {
        val part = MultipartBody.Part.createFormData(
            "photo",
            photo.name,
            photo.asRequestBody(JPEG_MEDIA_TYPE.toMediaType()),
        )

        return try {
            val user = meApi.uploadAvatar(part).data.toDomain()
            Timber.d("uploadAvatar: success")
            AvatarResult.Success(user)
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNPROCESSABLE_ENTITY) {
                Timber.d("uploadAvatar: rejected by the backend")
                AvatarResult.Rejected
            } else {
                Timber.d("uploadAvatar: server error %d", e.code())
                AvatarResult.Error
            }
        } catch (e: IOException) {
            Timber.d(e, "uploadAvatar: network error, offline")
            AvatarResult.Offline
        }
    }

    override suspend fun removeAvatar(): AvatarResult {
        return try {
            val user = meApi.deleteAvatar().data.toDomain()
            Timber.d("removeAvatar: success")
            AvatarResult.Success(user)
        } catch (e: HttpException) {
            Timber.d("removeAvatar: server error %d", e.code())
            AvatarResult.Error
        } catch (e: IOException) {
            Timber.d(e, "removeAvatar: network error, offline")
            AvatarResult.Offline
        }
    }

    private companion object {
        const val HTTP_UNPROCESSABLE_ENTITY = 422
        const val JPEG_MEDIA_TYPE = "image/jpeg"
    }
}
