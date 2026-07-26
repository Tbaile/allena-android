package it.bailettitommaso.fairly.data.session

import io.mockk.coEvery
import io.mockk.mockk
import it.bailettitommaso.fairly.data.remote.api.MeApi
import it.bailettitommaso.fairly.data.remote.dto.MeDto
import it.bailettitommaso.fairly.data.remote.dto.MeEnvelopeDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class CurrentUserStoreTest {

    private val meApi = mockk<MeApi>()
    private val store = CurrentUserStore(meApi)

    @Test
    fun `refresh maps me response`() = runTest {
        coEvery { meApi.me() } returns MeEnvelopeDto(
            MeDto(id = 1, name = "Jane", email = "jane@example.com", role = "expert", mustChangePassword = true),
        )

        val result = store.refresh()

        assertEquals("Jane", result?.name)
        assertEquals(true, result?.mustChangePassword)
    }

    @Test
    fun `refresh returning failure yields null`() = runTest {
        coEvery { meApi.me() } throws IOException("no network")

        assertNull(store.refresh())
    }
}
