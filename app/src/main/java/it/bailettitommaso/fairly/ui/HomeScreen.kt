package it.bailettitommaso.fairly.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.fairly.ui.components.FairlyButton
import it.bailettitommaso.fairly.ui.components.PlaceholderScreen
import it.bailettitommaso.fairly.ui.me.ProfileViewModel
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

/** Authenticated landing for every role: a placeholder plus a logout action. */
@Composable
fun HomeScreen(onLoggedOut: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLoggedOut()
    }

    HomeContent(onLogout = viewModel::logout)
}

@Composable
private fun HomeContent(onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        PlaceholderScreen(title = "Fairly", subtitle = "Coming soon")
        FairlyButton(
            text = "Log out",
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    FairlyTheme {
        HomeContent(onLogout = {})
    }
}
