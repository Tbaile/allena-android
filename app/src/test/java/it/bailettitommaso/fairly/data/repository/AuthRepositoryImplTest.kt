package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.data.remote.api.AuthApi
import it.bailettitommaso.fairly.data.remote.dto.LoginResponseDto
import it.bailettitommaso.fairly.data.remote.dto.UserDto
import it.bailettitommaso.fairly.domain.repository.LoginResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryImplTest {

    private val authApi = mockk<AuthApi>()
    private val tokenStore = mockk<TokenStore>(relaxed = true)
    private val repository = AuthRepositoryImpl(authApi, tokenStore)

    @Test
    fun `successful login persists token and returns Success`() = runTest {
        coEvery { authApi.login(any()) } returns LoginResponseDto(
            token = "abc123",
            user = UserDto(id = 1, name = "Jane", email = "jane@example.com", role = "client"),
        )

        val result = repository.login("jane@example.com", "secret")

        assertTrue(result is LoginResult.Success)
        coVerify { tokenStore.save("abc123") }
    }

    @Test
    fun `422 maps to InvalidCredentials and does not save a token`() = runTest {
        coEvery { authApi.login(any()) } throws http(422)

        val result = repository.login("jane@example.com", "wrong")

        assertEquals(LoginResult.InvalidCredentials, result)
        coVerify(exactly = 0) { tokenStore.save(any()) }
    }

    @Test
    fun `network failure maps to Offline`() = runTest {
        coEvery { authApi.login(any()) } throws IOException("no network")

        val result = repository.login("jane@example.com", "secret")

        assertEquals(LoginResult.Offline, result)
    }

    @Test
    fun `unexpected status maps to Error`() = runTest {
        coEvery { authApi.login(any()) } throws http(500)

        val result = repository.login("jane@example.com", "secret")

        assertEquals(LoginResult.Error, result)
    }

    @Test
    fun `logout clears token on success`() = runTest {
        coEvery { authApi.logout() } returns Unit

        repository.logout()

        coVerify { tokenStore.clear() }
    }

    @Test
    fun `logout clears token even when server call fails`() = runTest {
        coEvery { authApi.logout() } throws IOException("no network")

        repository.logout()

        coVerify { tokenStore.clear() }
    }

    private fun http(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
