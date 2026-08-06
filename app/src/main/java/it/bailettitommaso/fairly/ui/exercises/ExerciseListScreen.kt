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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.ui.components.ErrorText
import it.bailettitommaso.fairly.ui.components.FairlyTextField
import it.bailettitommaso.fairly.ui.theme.FairlyTheme
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

@Composable
fun ExercisesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exercises = viewModel.exercises.collectAsLazyPagingItems()
    ExercisesContent(
        state = state,
        exercises = exercises,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        modifier = modifier,
    )
}

@Composable
private fun ExercisesContent(
    state: ExercisesUiState,
    exercises: LazyPagingItems<Exercise>,
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

        val refreshState = exercises.loadState.refresh
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                refreshState is LoadState.Loading -> CircularProgressIndicator()
                refreshState is LoadState.Error -> ErrorText(message = refreshState.error.toDisplayMessage())
                exercises.itemCount == 0 -> Text(
                    text = "No exercises found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> ExerciseList(exercises = exercises)
            }
        }
    }
}

@Composable
private fun ExerciseList(exercises: LazyPagingItems<Exercise>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(count = exercises.itemCount, key = exercises.itemKey { it.id }) { index ->
            val exercise = exercises[index] ?: return@items
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    supportingContent = exercise.category?.let { { Text(it.name) } },
                )
            }
        }

        when (val append = exercises.loadState.append) {
            is LoadState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
            is LoadState.Error -> item {
                ErrorText(message = append.error.toDisplayMessage(), modifier = Modifier.fillMaxWidth())
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

private fun Throwable.toDisplayMessage(): String = when (this) {
    is IOException -> "You appear to be offline. Check your connection."
    else -> "Something went wrong. Please try again."
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

@Composable
private fun previewPagingItems(exercises: List<Exercise>) =
    flowOf(PagingData.from(exercises)).collectAsLazyPagingItems()

@Preview(showBackground = true)
@Composable
private fun ExercisesContentPreview() {
    FairlyTheme {
        ExercisesContent(
            state = ExercisesUiState(categories = previewCategories),
            exercises = previewPagingItems(previewExercises),
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
                categories = previewCategories,
                selectedCategorySlug = "strength",
            ),
            exercises = previewPagingItems(previewExercises),
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
            state = ExercisesUiState(categories = previewCategories),
            exercises = previewPagingItems(emptyList()),
            onQueryChange = {},
            onCategorySelect = {},
        )
    }
}
