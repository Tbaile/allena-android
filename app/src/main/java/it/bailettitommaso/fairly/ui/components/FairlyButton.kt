package it.bailettitommaso.fairly.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun FairlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 8.dp),
                strokeWidth = 2.dp,
            )
        }
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyButtonPreview() {
    FairlyTheme {
        FairlyButton(text = "Sign in", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyButtonDisabledPreview() {
    FairlyTheme {
        FairlyButton(text = "Sign in", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyButtonLoadingPreview() {
    FairlyTheme {
        FairlyButton(text = "Sign in", onClick = {}, loading = true)
    }
}
