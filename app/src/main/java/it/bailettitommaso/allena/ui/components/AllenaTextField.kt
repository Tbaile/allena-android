package it.bailettitommaso.allena.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.allena.ui.theme.AllenaTheme

private val TextFieldShape = RoundedCornerShape(10.dp)

@Composable
fun AllenaTextField(
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
        placeholder = { Text(label) },
        shape = TextFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
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
private fun AllenaTextFieldEmptyPreview() {
    AllenaTheme {
        AllenaTextField(value = "", onValueChange = {}, label = "Email")
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaTextFieldFilledPreview() {
    AllenaTheme {
        AllenaTextField(value = "jane@example.com", onValueChange = {}, label = "Email")
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaTextFieldPasswordPreview() {
    AllenaTheme {
        AllenaTextField(
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
private fun AllenaTextFieldErrorPreview() {
    AllenaTheme {
        AllenaTextField(value = "not-an-email", onValueChange = {}, label = "Email", isError = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun AllenaTextFieldDisabledPreview() {
    AllenaTheme {
        AllenaTextField(value = "jane@example.com", onValueChange = {}, label = "Email", enabled = false)
    }
}
