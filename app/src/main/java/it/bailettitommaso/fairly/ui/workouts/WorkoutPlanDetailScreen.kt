package it.bailettitommaso.fairly.ui.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.model.WorkoutPlanItem
import it.bailettitommaso.fairly.ui.components.OfflineCause
import it.bailettitommaso.fairly.ui.components.OfflineState
import it.bailettitommaso.fairly.ui.theme.FairlyTheme

@Composable
fun WorkoutPlanDetailScreen(
    onBack: () -> Unit,
    viewModel: WorkoutPlanDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutPlanDetailContent(state = state, onBack = onBack, onRetry = viewModel::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutPlanDetailContent(
    state: WorkoutPlanDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                WorkoutPlanDetailUiState.Loading -> CircularProgressIndicator()
                is WorkoutPlanDetailUiState.Success -> PlanDetails(state.plan)
                WorkoutPlanDetailUiState.NotFound -> OfflineState(
                    cause = OfflineCause.NOT_FOUND,
                    modifier = Modifier.fillMaxSize(),
                )
                WorkoutPlanDetailUiState.Offline -> OfflineState(
                    cause = OfflineCause.NETWORK,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
                WorkoutPlanDetailUiState.Error -> OfflineState(
                    cause = OfflineCause.SERVER,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun PlanDetails(plan: WorkoutPlan) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = plan.name, style = MaterialTheme.typography.headlineSmall)
                plan.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(plan.items, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                PlanItemRow(item)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanItemRow(item: WorkoutPlanItem) {
    ListItem(
        overlineContent = { Text("${item.position}") },
        headlineContent = { Text(item.exercise.name) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(item.loadLabel()) })
                    AssistChip(onClick = {}, label = { Text("rest ${formatRest(item.restSeconds)}") })
                    item.targetWeight?.let { weight ->
                        AssistChip(onClick = {}, label = { Text(formatWeight(weight)) })
                    }
                }
                item.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlanDetailContentPreview() {
    FairlyTheme {
        WorkoutPlanDetailContent(
            state = WorkoutPlanDetailUiState.Success(previewPlan),
            onBack = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutPlanDetailContentDarkPreview() {
    FairlyTheme {
        WorkoutPlanDetailContent(
            state = WorkoutPlanDetailUiState.Success(previewPlan),
            onBack = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlanDetailContentOfflinePreview() {
    FairlyTheme {
        WorkoutPlanDetailContent(state = WorkoutPlanDetailUiState.Offline, onBack = {}, onRetry = {})
    }
}
