package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.MeDto
import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.fairly.domain.repository.ChangePasswordResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
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
}
