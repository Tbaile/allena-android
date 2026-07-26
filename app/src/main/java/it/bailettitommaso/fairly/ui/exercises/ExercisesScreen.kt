package it.bailettitommaso.fairly.ui.exercises

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.bailettitommaso.fairly.ui.components.PlaceholderScreen
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun ExercisesScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(title = "Exercises", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun ExercisesScreenPreview() {
    FairlyTheme {
        ExercisesScreen()
    }
}
