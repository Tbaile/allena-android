package it.bailettitommaso.allena.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.allena.ui.theme.AllenaTheme

private val ButtonShape = RoundedCornerShape(14.dp)
private val ButtonContentPadding = PaddingValues(vertical = 16.dp)

@Composable
fun AllenaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = ButtonShape,
        contentPadding = ButtonContentPadding,
        modifier = modifier,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
        }
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaButtonPreview() {
    AllenaTheme {
        AllenaButton(text = "Sign in", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaButtonDisabledPreview() {
    AllenaTheme {
        AllenaButton(text = "Sign in", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaButtonLoadingPreview() {
    AllenaTheme {
        AllenaButton(text = "Sign in", onClick = {}, loading = true)
    }
}
