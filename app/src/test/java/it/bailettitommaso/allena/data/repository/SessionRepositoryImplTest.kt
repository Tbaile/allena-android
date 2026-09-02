package it.bailettitommaso.allena.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.bailettitommaso.allena.data.local.TokenStore
import it.bailettitommaso.allena.data.remote.api.MeApi
import it.bailettitommaso.allena.data.remote.dto.MeDto
import it.bailettitommaso.allena.data.remote.dto.MeEnvelopeDto
import it.bailettitommaso.allena.domain.repository.SessionResult
import it.bailettitommaso.allena.util.ConnectivityObserver
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
    private val connectivityObserver = mockk<ConnectivityObserver>()
    private val repository = SessionRepositoryImpl(tokenStore, meApi, connectivityObserver)

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
        assertEquals("Jane", (result as SessionResult.Authenticated).user.name)
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
    fun `network failure while offline yields Unreachable NETWORK`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        coEvery { meApi.me() } throws IOException("no network")
        every { connectivityObserver.isOnline() } returns false

        val result = repository.bootstrap()

        assertEquals(SessionResult.Unreachable(SessionResult.Unreachable.Cause.NETWORK), result)
    }

    @Test
    fun `connection failure while online yields Unreachable SERVER`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        coEvery { meApi.me() } throws IOException("connection refused")
        every { connectivityObserver.isOnline() } returns true

        val result = repository.bootstrap()

        assertEquals(SessionResult.Unreachable(SessionResult.Unreachable.Cause.SERVER), result)
    }

    @Test
    fun `server error yields Unreachable SERVER without consulting connectivity`() = runTest {
        coEvery { tokenStore.currentToken() } returns "token"
        coEvery { meApi.me() } throws http(500)

        val result = repository.bootstrap()

        assertEquals(SessionResult.Unreachable(SessionResult.Unreachable.Cause.SERVER), result)
        verify(exactly = 0) { connectivityObserver.isOnline() }
    }

    private fun http(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))
}
