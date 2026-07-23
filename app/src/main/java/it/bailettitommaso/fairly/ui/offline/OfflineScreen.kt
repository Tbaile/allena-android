package it.bailettitommaso.fairly.ui.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun OfflineScreen(onRetry: () -> Unit) {
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
                text = "You're offline",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "We couldn't reach Fairly. We'll retry automatically when you're back online.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineScreenPreview() {
    FairlyTheme {
        OfflineScreen(onRetry = {})
    }
}
