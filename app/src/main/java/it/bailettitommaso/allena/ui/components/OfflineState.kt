package it.bailettitommaso.allena.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.allena.ui.theme.AllenaTheme
import java.io.IOException

/** Why content can't be shown. Single source of the copy used by [OfflineState]. */
enum class OfflineCause(val title: String, val message: String) {
    NETWORK("You're offline", "Check your connection and try again."),
    SERVER("Can't reach Allena", "The service isn't responding right now. Try again in a moment."),
    NOT_FOUND("Not found", "This content is no longer available."),
}

fun Throwable.toOfflineCause(): OfflineCause =
    if (this is IOException) OfflineCause.NETWORK else OfflineCause.SERVER

/**
 * Empty state for content that could not be loaded. Fills whatever it is given: the whole screen at
 * boot, the content area of the hosting view once inside the app. Omit [onRetry] when there is
 * nothing to retry (a missing resource).
 */
@Composable
fun OfflineState(
    cause: OfflineCause,
    modifier: Modifier = Modifier,
    retrying: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = cause.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = cause.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (onRetry != null) {
            AllenaButton(
                text = "Retry",
                onClick = onRetry,
                loading = retrying,
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineStateNetworkPreview() {
    AllenaTheme {
        OfflineState(
            cause = OfflineCause.NETWORK,
            modifier = Modifier.fillMaxSize(),
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineStateServerRetryingPreview() {
    AllenaTheme {
        OfflineState(
            cause = OfflineCause.SERVER,
            modifier = Modifier.fillMaxSize(),
            retrying = true,
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineStateNotFoundPreview() {
    AllenaTheme {
        OfflineState(cause = OfflineCause.NOT_FOUND, modifier = Modifier.fillMaxSize())
    }
}
