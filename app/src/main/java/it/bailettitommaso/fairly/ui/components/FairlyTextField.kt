package it.bailettitommaso.fairly.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun FairlyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FairlyTextFieldEmptyPreview() {
    FairlyTheme {
        FairlyTextField(value = "", onValueChange = {}, label = "Email")
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyTextFieldFilledPreview() {
    FairlyTheme {
        FairlyTextField(value = "jane@example.com", onValueChange = {}, label = "Email")
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyTextFieldPasswordPreview() {
    FairlyTheme {
        FairlyTextField(
            value = "secret",
            onValueChange = {},
            label = "Password",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyTextFieldErrorPreview() {
    FairlyTheme {
        FairlyTextField(value = "not-an-email", onValueChange = {}, label = "Email", isError = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun FairlyTextFieldDisabledPreview() {
    FairlyTheme {
        FairlyTextField(value = "jane@example.com", onValueChange = {}, label = "Email", enabled = false)
    }
}
