package it.bailettitommaso.fairly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import it.bailettitommaso.fairly.domain.model.ThemeMode
import it.bailettitommaso.fairly.ui.boot.BootState
import it.bailettitommaso.fairly.ui.boot.BootViewModel
import it.bailettitommaso.fairly.ui.navigation.FairlyNavGraph
import it.bailettitommaso.fairly.ui.settings.ThemeViewModel
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bootViewModel: BootViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash until the boot session check resolves and the stored theme is known.
        splashScreen.setKeepOnScreenCondition {
            bootViewModel.state.value is BootState.Loading || themeViewModel.mode.value == null
        }
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.mode.collectAsStateWithLifecycle()
            FairlyTheme(themeMode = themeMode ?: ThemeMode.SYSTEM) {
                FairlyNavGraph(bootViewModel = bootViewModel)
            }
        }
    }
}
