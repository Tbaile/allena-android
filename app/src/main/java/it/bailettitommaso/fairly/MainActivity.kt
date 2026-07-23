package it.bailettitommaso.fairly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import it.bailettitommaso.fairly.ui.boot.BootState
import it.bailettitommaso.fairly.ui.boot.BootViewModel
import it.bailettitommaso.fairly.ui.navigation.FairlyNavGraph
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bootViewModel: BootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash exactly until the boot session check resolves.
        splashScreen.setKeepOnScreenCondition {
            bootViewModel.state.value is BootState.Loading
        }
        enableEdgeToEdge()
        setContent {
            FairlyTheme {
                FairlyNavGraph(bootViewModel = bootViewModel)
            }
        }
    }
}
