package it.bailettitommaso.allena.ui.exercises

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import it.bailettitommaso.allena.ui.theme.AllenaTheme

/**
 * What an exercise is, shared by the detail screen and the player's info sheet. Padding and
 * scrolling are the caller's, since a full screen and a bottom sheet want different insets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseSummary(exercise: Exercise, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = exercise.name, style = MaterialTheme.typography.headlineSmall)

        if (exercise.category != null || exercise.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                exercise.category?.let { category ->
                    AssistChip(onClick = {}, label = { Text(category.name) })
                }
                exercise.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag.name) })
                }
            }
        }

        Text(text = exercise.description, style = MaterialTheme.typography.bodyMedium)
    }
}

/** [ExerciseSummary] as an overlay, so a workout screen can explain an exercise without navigating. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoSheet(exercise: Exercise, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        ExerciseSummary(
            exercise = exercise,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        )
    }
}

internal val previewExercise = Exercise(
    id = 1,
    name = "Barbell Back Squat",
    description = "Rest the barbell on your upper back, brace your core and sit down until your " +
        "thighs are parallel to the floor. Drive through the heels to stand back up.",
    category = Category(3, "Strength", "strength"),
    videoUrl = null,
    tags = listOf(Tag(1, "Legs", "legs"), Tag(2, "Compound", "compound"), Tag(3, "Barbell", "barbell")),
)

@Preview(showBackground = true)
@Composable
private fun ExerciseSummaryPreview() {
    AllenaTheme {
        ExerciseSummary(exercise = previewExercise, modifier = Modifier.padding(24.dp))
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExerciseSummaryDarkPreview() {
    AllenaTheme {
        ExerciseSummary(exercise = previewExercise, modifier = Modifier.padding(24.dp))
    }
}
