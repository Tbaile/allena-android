package it.bailettitommaso.fairly.ui.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Backing content for [it.bailettitommaso.fairly.ui.navigation.Route.Boot] while the boot
 * session check runs. Deliberately empty: the native splash screen (see
 * `installSplashScreen()`/`setKeepOnScreenCondition` in MainActivity) is the only loading
 * UI shown here, so this must not render anything that could flash underneath it.
 */
@Composable
fun BootScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
}
