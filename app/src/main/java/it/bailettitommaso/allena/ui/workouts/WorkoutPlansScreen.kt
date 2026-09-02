package it.bailettitommaso.allena.ui.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.ui.components.OfflineCause
import it.bailettitommaso.allena.ui.components.OfflineState
import it.bailettitommaso.allena.ui.theme.AllenaTheme

@Composable
fun WorkoutPlansScreen(
    onPlanClick: (Long) -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlansViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutPlansContent(
        state = state,
        onPlanClick = onPlanClick,
        onRetry = viewModel::retry,
        onHistoryClick = onHistoryClick,
        modifier = modifier,
    )
}

@Composable
private fun WorkoutPlansContent(
    state: WorkoutPlansUiState,
    onPlanClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Your workouts", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onHistoryClick) { Text("Progress") }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                WorkoutPlansUiState.Loading -> CircularProgressIndicator()
                WorkoutPlansUiState.Offline -> OfflineState(
                    cause = OfflineCause.NETWORK,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
                WorkoutPlansUiState.Error -> OfflineState(
                    cause = OfflineCause.SERVER,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
                is WorkoutPlansUiState.Success -> if (state.plans.isEmpty()) {
                    EmptyPlans()
                } else {
                    PlanList(plans = state.plans, onPlanClick = onPlanClick)
                }
            }
        }
    }
}

@Composable
private fun EmptyPlans() {
    Text(
        text = "No workouts assigned yet",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PlanList(plans: List<WorkoutPlan>, onPlanClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(plans, key = { it.id }) { plan ->
            Card(onClick = { onPlanClick(plan.id) }, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(plan.name) },
                    supportingContent = { Text(plan.summary()) },
                )
            }
        }
    }
}

/** "6 exercises · 20 sets" — enough to tell two plans apart without opening them. */
private fun WorkoutPlan.summary(): String {
    val exercises = if (items.size == 1) "1 exercise" else "${items.size} exercises"
    val sets = items.sumOf { it.sets }

    return "$exercises · $sets sets"
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlansContentPreview() {
    AllenaTheme {
        WorkoutPlansContent(
            state = WorkoutPlansUiState.Success(listOf(previewPlan, previewPlan.copy(id = 2, name = "Upper Body B"))),
            onPlanClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutPlansContentDarkPreview() {
    AllenaTheme {
        WorkoutPlansContent(
            state = WorkoutPlansUiState.Success(listOf(previewPlan)),
            onPlanClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlansContentEmptyPreview() {
    AllenaTheme {
        WorkoutPlansContent(state = WorkoutPlansUiState.Success(emptyList()), onPlanClick = {}, onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlansContentOfflinePreview() {
    AllenaTheme {
        WorkoutPlansContent(state = WorkoutPlansUiState.Offline, onPlanClick = {}, onRetry = {})
    }
}
