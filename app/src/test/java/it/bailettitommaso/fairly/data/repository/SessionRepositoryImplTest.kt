package it.bailettitommaso.fairly.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.bailettitommaso.fairly.data.local.TokenStore
import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.MeDto
import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.fairly.domain.model.Role
import it.bailettitommaso.fairly.domain.repository.SessionResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SessionRepositoryImplTest {

    private val tokenStore = mockk<TokenStore>(relaxed = true)
    private val meApi = mockk<MeApi>()
    private val repository = SessionRepositoryImpl(tokenStore, meApi)

    @Test
    fun `no token yields Unauthenticated without hitting the network`() = runTest {
        coEvery { tokenStore.currentToken() } returns null

        val result = repository.bootstrap()

        assertEquals(SessionResult.Unauthenticated, result)
        coVerify(exactly = 0) { meApi.me() }
    }

    @Test
    fun `valid token maps me response to Authenticated`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        coEvery { meApi.me() } returns MeEnvelopeDto(
            MeDto(id = 1, name = "Jane", email = "jane@example.com", role = "expert"),
        )

        val result = repository.bootstrap()

        assertTrue(result is SessionResult.Authenticated)
        assertEquals(Role.EXPERT, (result as SessionResult.Authenticated).user.role)
    }

    @Test
    fun `401 clears token and yields Unauthenticated`() = runTest {
        coEvery { tokenStore.currentToken() } returns "stale"
        coEvery { meApi.me() } throws http(401)

        val result = repository.bootstrap()

        assertEquals(SessionResult.Unauthenticated, result)
        coVerify { tokenStore.clear() }
    }

    @Test
    fun `network failure yields Offline`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        coEvery { meApi.me() } throws IOException("no network")

        val result = repository.bootstrap()

        assertEquals(SessionResult.Offline, result)
    }

    private fun http(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
