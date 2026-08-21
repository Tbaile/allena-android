package it.bailettitommaso.fairly.ui.favorites

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun FavoritesScreen(
    onExerciseClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    FavoritesContent(
        favorites = favorites,
        onExerciseClick = onExerciseClick,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}

@Composable
private fun FavoritesContent(
    favorites: List<Exercise>,
    onExerciseClick: (Long) -> Unit,
    onToggleFavorite: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (favorites.isEmpty()) {
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                items(favorites, key = { it.id }) { exercise ->
                    Card(
                        onClick = { onExerciseClick(exercise.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = exercise.category?.let { { Text(it.name) } },
                            trailingContent = {
                                IconButton(onClick = { onToggleFavorite(exercise) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = "Remove from favorites")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private val previewFavorites = listOf(
    Exercise(1, "Barbell Back Squat", "", Category(3, "Strength", "strength"), null, isFavorite = true),
    Exercise(2, "Downward Dog", "", Category(1, "Yoga", "yoga"), null, isFavorite = true),
)

@Preview(showBackground = true)
@Composable
private fun FavoritesContentPreview() {
    FairlyTheme {
        FavoritesContent(favorites = previewFavorites, onExerciseClick = {}, onToggleFavorite = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FavoritesContentDarkPreview() {
    FairlyTheme {
        FavoritesContent(favorites = previewFavorites, onExerciseClick = {}, onToggleFavorite = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesContentEmptyPreview() {
    FairlyTheme {
        FavoritesContent(favorites = emptyList(), onExerciseClick = {}, onToggleFavorite = {})
    }
}
