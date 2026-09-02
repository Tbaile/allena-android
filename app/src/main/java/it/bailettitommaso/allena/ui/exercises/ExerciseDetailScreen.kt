package it.bailettitommaso.allena.ui.exercises

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.model.Tag
import it.bailettitommaso.allena.ui.components.OfflineCause
import it.bailettitommaso.allena.ui.components.OfflineState
import it.bailettitommaso.allena.ui.theme.AllenaTheme

@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExerciseDetailContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailContent(
    state: ExerciseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is ExerciseDetailUiState.Success) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (state.exercise.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (state.exercise.isFavorite) "Remove from favorites" else "Add to favorites",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ExerciseDetailUiState.Loading -> CircularProgressIndicator()
                is ExerciseDetailUiState.Success -> ExerciseDetails(state.exercise)
                ExerciseDetailUiState.NotFound -> OfflineState(
                    cause = OfflineCause.NOT_FOUND,
                    modifier = Modifier.fillMaxSize(),
                )
                ExerciseDetailUiState.Offline -> OfflineState(
                    cause = OfflineCause.NETWORK,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
                ExerciseDetailUiState.Error -> OfflineState(
                    cause = OfflineCause.SERVER,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetails(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
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

private val previewExercise = Exercise(
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
private fun ExerciseDetailContentPreview() {
    AllenaTheme {
        ExerciseDetailContent(
            state = ExerciseDetailUiState.Success(previewExercise),
            onBack = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExerciseDetailContentDarkPreview() {
    AllenaTheme {
        ExerciseDetailContent(
            state = ExerciseDetailUiState.Success(previewExercise),
            onBack = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailContentOfflinePreview() {
    AllenaTheme {
        ExerciseDetailContent(
            state = ExerciseDetailUiState.Offline,
            onBack = {},
            onRetry = {},
        )
    }
}
