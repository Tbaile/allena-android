package it.bailettitommaso.allena.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.allena.data.remote.api.MeApi
import it.bailettitommaso.allena.data.remote.dto.MeDto
import it.bailettitommaso.allena.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.allena.domain.repository.AvatarResult
import it.bailettitommaso.allena.domain.repository.ChangePasswordResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException

class MeRepositoryImplTest {

    private val meApi = mockk<MeApi>()
    private val repository = MeRepositoryImpl(meApi)

    @Test
    fun `successful update returns Success`() = runTest {
        coEvery { meApi.update(any()) } returns MeEnvelopeDto(
            data = MeDto(id = 1, name = "Jane", email = "jane@example.com"),
        )

        val result = repository.changePassword(
            currentPassword = "current",
            password = "newpassword123",
            passwordConfirmation = "newpassword123",
        )

        assertEquals(ChangePasswordResult.Success, result)
    }

    @Test
    fun `422 maps to InvalidCurrentPassword`() = runTest {
        coEvery { meApi.update(any()) } throws http(422)

        val result = repository.changePassword(
            currentPassword = "wrong",
            password = "newpassword123",
            passwordConfirmation = "newpassword123",
        )

        assertEquals(ChangePasswordResult.InvalidCurrentPassword, result)
    }

    @Test
    fun `unexpected status maps to Error`() = runTest {
        coEvery { meApi.update(any()) } throws http(500)

        val result = repository.changePassword(
            currentPassword = "current",
            password = "newpassword123",
            passwordConfirmation = "newpassword123",
        )

        assertEquals(ChangePasswordResult.Error, result)
    }

    @Test
    fun `network failure maps to Offline`() = runTest {
        coEvery { meApi.update(any()) } throws IOException("no network")

        val result = repository.changePassword(
            currentPassword = "current",
            password = "newpassword123",
            passwordConfirmation = "newpassword123",
        )

        assertEquals(ChangePasswordResult.Offline, result)
    }

    private fun http(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))

    @Test
    fun `a successful avatar upload returns the updated user`() = runTest {
        coEvery { meApi.uploadAvatar(any()) } returns MeEnvelopeDto(
            data = MeDto(
                id = 1,
                name = "Jane",
                email = "jane@example.com",
                avatarUrl = "http://host/api/v1/me/avatar?v=abc",
            ),
        )

        val result = repository.uploadAvatar(photo())

        assertEquals(
            AvatarResult.Success(
                it.bailettitommaso.allena.domain.model.User(
                    id = 1,
                    name = "Jane",
                    email = "jane@example.com",
                    mustChangePassword = false,
                    avatarUrl = "http://host/api/v1/me/avatar?v=abc",
                ),
            ),
            result,
        )
    }

    @Test
    fun `422 on upload maps to Rejected`() = runTest {
        coEvery { meApi.uploadAvatar(any()) } throws http(422)

        assertEquals(AvatarResult.Rejected, repository.uploadAvatar(photo()))
    }

    @Test
    fun `a network failure on upload maps to Offline`() = runTest {
        coEvery { meApi.uploadAvatar(any()) } throws IOException("no network")

        assertEquals(AvatarResult.Offline, repository.uploadAvatar(photo()))
    }

    @Test
    fun `removing the avatar returns the updated user`() = runTest {
        coEvery { meApi.deleteAvatar() } returns MeEnvelopeDto(
            data = MeDto(id = 1, name = "Jane", email = "jane@example.com"),
        )

        val result = repository.removeAvatar()

        assertEquals(null, (result as AvatarResult.Success).user.avatarUrl)
    }

    private fun photo(): File = File.createTempFile("avatar", ".jpg").apply {
        writeBytes(byteArrayOf(1, 2, 3))
        deleteOnExit()
    }
}
