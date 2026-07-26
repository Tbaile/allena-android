package it.bailettitommaso.fairly.ui.exercises

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.ui.components.ErrorText
import it.bailettitommaso.fairly.ui.components.FairlyTextField
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun ExercisesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExercisesContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        modifier = modifier,
    )
}

@Composable
private fun ExercisesContent(
    state: ExercisesUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FairlyTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = "Search exercises",
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedCategorySlug == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All") },
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = state.selectedCategorySlug == category.slug,
                        onClick = { onCategorySelect(category.slug) },
                        label = { Text(category.name) },
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> ErrorText(message = state.error.message())
                state.exercises.isEmpty() -> Text(
                    text = "No exercises found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> ExerciseList(exercises = state.exercises)
            }
        }
    }
}

@Composable
private fun ExerciseList(exercises: List<Exercise>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(exercises, key = { it.id }) { exercise ->
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    supportingContent = exercise.category?.let { { Text(it.name) } },
                )
            }
        }
    }
}

private fun ExercisesError.message(): String = when (this) {
    ExercisesError.OFFLINE -> "You appear to be offline. Check your connection."
    ExercisesError.GENERIC -> "Something went wrong. Please try again."
}

private val previewExercises = listOf(
    Exercise(1, "Barbell Back Squat", "", Category(3, "Strength", "strength"), null),
    Exercise(2, "Downward Dog", "", Category(1, "Yoga", "yoga"), null),
    Exercise(3, "Jumping Jacks", "", Category(4, "Cardio", "cardio"), null),
)

private val previewCategories = listOf(
    Category(1, "Yoga", "yoga"),
    Category(2, "Free Body", "free-body"),
    Category(3, "Strength", "strength"),
    Category(4, "Cardio", "cardio"),
)

@Preview(showBackground = true)
@Composable
private fun ExercisesContentPreview() {
    FairlyTheme {
        ExercisesContent(
            state = ExercisesUiState(
                isLoading = false,
                exercises = previewExercises,
                categories = previewCategories,
            ),
            onQueryChange = {},
            onCategorySelect = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExercisesContentDarkPreview() {
    FairlyTheme {
        ExercisesContent(
            state = ExercisesUiState(
                isLoading = false,
                exercises = previewExercises,
                categories = previewCategories,
                selectedCategorySlug = "strength",
            ),
            onQueryChange = {},
            onCategorySelect = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExercisesContentEmptyPreview() {
    FairlyTheme {
        ExercisesContent(
            state = ExercisesUiState(isLoading = false, categories = previewCategories),
            onQueryChange = {},
            onCategorySelect = {},
        )
    }
}
