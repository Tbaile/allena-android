package it.bailettitommaso.allena.ui.offline

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.bailettitommaso.allena.util.ConnectivityObserver
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConnectivityViewModelTest {

    private val observer = mockk<ConnectivityObserver>()

    @Test
    fun `seeded AVAILABLE is not a reconnection`() = runTest {
        every { observer.status } returns flowOf(ConnectivityObserver.Status.AVAILABLE)

        ConnectivityViewModel(observer).reconnected.test {
            awaitComplete()
        }
    }

    @Test
    fun `a real transition back to AVAILABLE emits once`() = runTest {
        every { observer.status } returns flowOf(
            ConnectivityObserver.Status.AVAILABLE,
            ConnectivityObserver.Status.UNAVAILABLE,
            ConnectivityObserver.Status.AVAILABLE,
        )

        ConnectivityViewModel(observer).reconnected.test {
            awaitItem()
            awaitComplete()
        }
    }
}
