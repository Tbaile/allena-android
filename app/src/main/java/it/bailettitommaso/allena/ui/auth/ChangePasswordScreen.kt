package it.bailettitommaso.allena.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.allena.ui.components.AllenaButton
import it.bailettitommaso.allena.ui.theme.AllenaTheme

@Composable
fun ChangePasswordScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) {
        if (state.done) onDone()
    }

    ChangePasswordContent(
        state = state,
        onBack = onBack,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordConfirmationChange = viewModel::onPasswordConfirmationChange,
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordContent(
    state: ChangePasswordUiState,
    onBack: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        topBar = {
            if (!state.forced) {
                TopAppBar(
                    title = { Text("Change password") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.forced) {
                Text(
                    text = "Set a new password",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "You must change your password before continuing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            ChangePasswordForm(
                currentPassword = state.currentPassword,
                onCurrentPasswordChange = onCurrentPasswordChange,
                password = state.password,
                onPasswordChange = onPasswordChange,
                passwordConfirmation = state.passwordConfirmation,
                onPasswordConfirmationChange = onPasswordConfirmationChange,
                requireCurrentPassword = state.requireCurrentPassword,
                enabled = !state.isSubmitting,
                error = state.error?.message(),
                modifier = Modifier.padding(top = 16.dp),
            )

            AllenaButton(
                text = if (state.forced) "Continue" else "Save",
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

private fun ChangePasswordError.message(): String = when (this) {
    ChangePasswordError.INVALID_CURRENT_PASSWORD -> "The current password is incorrect."
    ChangePasswordError.TOO_SHORT -> "Password must be at least 8 characters."
    ChangePasswordError.MISMATCH -> "Passwords don't match."
    ChangePasswordError.OFFLINE -> "No connection. Check your network and try again."
    ChangePasswordError.GENERIC -> "Something went wrong. Please try again."
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordContentForcedPreview() {
    AllenaTheme {
        ChangePasswordContent(
            state = ChangePasswordUiState(forced = true),
            onBack = {},
            onCurrentPasswordChange = {},
            onPasswordChange = {},
            onPasswordConfirmationChange = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordContentVoluntaryPreview() {
    AllenaTheme {
        ChangePasswordContent(
            state = ChangePasswordUiState(forced = false),
            onBack = {},
            onCurrentPasswordChange = {},
            onPasswordChange = {},
            onPasswordConfirmationChange = {},
            onSubmit = {},
        )
    }
}
