package it.bailettitommaso.allena.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.allena.ui.components.ErrorText
import it.bailettitommaso.allena.ui.components.AllenaTextField
import it.bailettitommaso.allena.ui.theme.AllenaTheme

/** Shared password fields for both the forced (post-login) and voluntary (profile) change-password flows. */
@Composable
fun ChangePasswordForm(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirmation: String,
    onPasswordConfirmationChange: (String) -> Unit,
    requireCurrentPassword: Boolean,
    enabled: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (requireCurrentPassword) {
            AllenaTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = "Current password",
                enabled = enabled,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AllenaTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "New password",
            enabled = enabled,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        AllenaTextField(
            value = passwordConfirmation,
            onValueChange = onPasswordConfirmationChange,
            label = "Confirm new password",
            enabled = enabled,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        if (error != null) {
            ErrorText(message = error, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordFormForcedPreview() {
    AllenaTheme {
        ChangePasswordForm(
            currentPassword = "",
            onCurrentPasswordChange = {},
            password = "",
            onPasswordChange = {},
            passwordConfirmation = "",
            onPasswordConfirmationChange = {},
            requireCurrentPassword = false,
            enabled = true,
            error = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordFormVoluntaryErrorPreview() {
    AllenaTheme {
        ChangePasswordForm(
            currentPassword = "wrong",
            onCurrentPasswordChange = {},
            password = "newpassword123",
            onPasswordChange = {},
            passwordConfirmation = "newpassword123",
            onPasswordConfirmationChange = {},
            requireCurrentPassword = true,
            enabled = true,
            error = "The current password is incorrect.",
        )
    }
}
