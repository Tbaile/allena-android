package it.bailettitommaso.fairly.ui.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.fairly.domain.repository.SessionResult.Unreachable.Cause
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun OfflineScreen(
    cause: Cause,
    retrying: Boolean,
    onRetry: () -> Unit,
) {
    val title = when (cause) {
        Cause.NETWORK -> "You're offline"
        Cause.SERVER -> "Can't reach Fairly"
    }
    val message = when (cause) {
        Cause.NETWORK -> "Check your connection. We'll retry automatically when you're back online."
        Cause.SERVER -> "The service isn't responding right now. Try again in a moment."
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onRetry,
                enabled = !retrying,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                if (retrying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                } else {
                    Text("Retry")
                }
            }
        }
    }
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
