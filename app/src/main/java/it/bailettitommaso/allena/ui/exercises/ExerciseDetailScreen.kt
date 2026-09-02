package it.bailettitommaso.allena.ui.exercises

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
                is ExerciseDetailUiState.Success -> ExerciseSummary(
                    exercise = state.exercise,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                )
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
