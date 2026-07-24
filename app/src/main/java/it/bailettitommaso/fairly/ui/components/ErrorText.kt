package it.bailettitommaso.fairly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorTextPreview() {
    FairlyTheme {
        ErrorText(
            message = "The provided credentials are incorrect.",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
