package it.bailettitommaso.fairly.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import it.bailettitommaso.fairly.ui.exercises.ExercisesScreen
import it.bailettitommaso.fairly.ui.me.ProfileScreen
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

private enum class HomeTab(val label: String, val icon: ImageVector) {
    Exercises("Exercises", Icons.AutoMirrored.Filled.List),
    Profile("Profile", Icons.Filled.Person),
}

/** Authenticated shell: a bottom navigation bar hosting the top-level sections. */
@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onChangePassword: () -> Unit = {},
    onExerciseClick: (Long) -> Unit = {},
) {
    var selected by rememberSaveable { mutableStateOf(HomeTab.Exercises) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (selected) {
            HomeTab.Exercises -> ExercisesScreen(
                onExerciseClick = onExerciseClick,
                modifier = contentModifier,
            )
            HomeTab.Profile -> ProfileScreen(
                onLogout = onLoggedOut,
                onChangePassword = onChangePassword,
                modifier = contentModifier,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FairlyTheme {
        HomeScreen(onLoggedOut = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    FairlyTheme {
        HomeScreen(onLoggedOut = {})
    }
}
