package it.bailettitommaso.fairly.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.fairly.ui.components.ErrorText
import it.bailettitommaso.fairly.ui.components.FairlyButton
import it.bailettitommaso.fairly.ui.components.FairlyTextField
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sessionExpired by viewModel.sessionExpired.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            snackbarHostState.showSnackbar(SESSION_EXPIRED_MESSAGE)
            viewModel.consumeSessionExpired()
        }
    }

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
    )
}

private const val SESSION_EXPIRED_MESSAGE = "Your session has expired. Please sign in again."

@Composable
private fun LoginContent(
    state: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Fairly",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            FairlyTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            FairlyTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                enabled = !state.isSubmitting,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            state.error?.let { error ->
                ErrorText(
                    message = error.message(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            FairlyButton(
                text = "Sign in",
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            )
        }
    }
}

private fun LoginError.message(): String = when (this) {
    LoginError.INVALID_CREDENTIALS -> "The provided credentials are incorrect."
    LoginError.OFFLINE -> "No connection. Check your network and try again."
    LoginError.GENERIC -> "Something went wrong. Please try again."
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    FairlyTheme {
        LoginContent(
            state = LoginUiState(email = "jane@example.com", password = "secret"),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentErrorPreview() {
    FairlyTheme {
        LoginContent(
            state = LoginUiState(
                email = "jane@example.com",
                password = "wrong",
                error = LoginError.INVALID_CREDENTIALS,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
        )
    }
}
