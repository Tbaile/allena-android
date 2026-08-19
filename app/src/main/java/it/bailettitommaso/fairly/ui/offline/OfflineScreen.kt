package it.bailettitommaso.fairly.ui.offline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.bailettitommaso.fairly.domain.repository.SessionResult.Unreachable.Cause
import it.bailettitommaso.fairly.ui.components.OfflineCause
import it.bailettitommaso.fairly.ui.components.OfflineState
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

/** Boot-time dead end: the session check could not reach the backend, so nothing else can render. */
@Composable
fun OfflineScreen(
    cause: Cause,
    retrying: Boolean,
    onRetry: () -> Unit,
) {
    Scaffold { padding ->
        OfflineState(
            cause = cause.toOfflineCause(),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            retrying = retrying,
            onRetry = onRetry,
        )
    }
}

private fun Cause.toOfflineCause(): OfflineCause = when (this) {
    Cause.NETWORK -> OfflineCause.NETWORK
    Cause.SERVER -> OfflineCause.SERVER
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenNoNetworkPreview() {
    FairlyTheme {
        OfflineScreen(cause = Cause.NETWORK, retrying = false, onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenServerDownPreview() {
    FairlyTheme {
        OfflineScreen(cause = Cause.SERVER, retrying = true, onRetry = {})
    }
}
