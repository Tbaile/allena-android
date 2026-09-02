package it.bailettitommaso.allena.ui.workouts

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.bailettitommaso.allena.ui.components.AllenaButton
import it.bailettitommaso.allena.ui.components.AllenaTextField
import it.bailettitommaso.allena.ui.components.OfflineCause
import it.bailettitommaso.allena.ui.components.OfflineState
import it.bailettitommaso.allena.ui.theme.AllenaTheme

@Composable
fun WorkoutPlayerScreen(
    onExit: () -> Unit,
    viewModel: WorkoutPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutPlayerContent(
        state = state,
        onEntryChange = viewModel::onEntryChange,
        onWeightChange = viewModel::onWeightChange,
        onCompleteSet = viewModel::completeSet,
        onSkipRest = viewModel::skipRest,
        onFinishEarly = viewModel::finishEarly,
        onDiscard = {
            viewModel.discard()
            onExit()
        },
        onRetry = viewModel::retry,
        onExit = onExit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutPlayerContent(
    state: WorkoutPlayerUiState,
    onEntryChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onCompleteSet: () -> Unit,
    onSkipRest: () -> Unit,
    onFinishEarly: () -> Unit,
    onDiscard: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
) {
    var confirmingDiscard by remember { mutableStateOf(false) }
    val running = state as? WorkoutPlayerUiState.Running

    // Leaving mid-workout throws the session away, so make it deliberate.
    BackHandler(enabled = running != null) { confirmingDiscard = true }

    if (confirmingDiscard) {
        DiscardDialog(
            onConfirm = {
                confirmingDiscard = false
                onDiscard()
            },
            onDismiss = { confirmingDiscard = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(running?.planName ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = { if (running != null) confirmingDiscard = true else onExit() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Discard workout")
                    }
                },
                actions = {
                    if (running != null) {
                        TextButton(onClick = onFinishEarly) { Text("Finish") }
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
                WorkoutPlayerUiState.Loading -> CircularProgressIndicator()
                is WorkoutPlayerUiState.Running -> RunningSet(
                    state = state,
                    onEntryChange = onEntryChange,
                    onWeightChange = onWeightChange,
                    onCompleteSet = onCompleteSet,
                    onSkipRest = onSkipRest,
                )
                is WorkoutPlayerUiState.Finished -> FinishedSummary(state = state, onExit = onExit)
                WorkoutPlayerUiState.NotFound -> OfflineState(
                    cause = OfflineCause.NOT_FOUND,
                    modifier = Modifier.fillMaxSize(),
                )
                WorkoutPlayerUiState.Offline -> OfflineState(
                    cause = OfflineCause.NETWORK,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
                WorkoutPlayerUiState.Error -> OfflineState(
                    cause = OfflineCause.SERVER,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun RunningSet(
    state: WorkoutPlayerUiState.Running,
    onEntryChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onCompleteSet: () -> Unit,
    onSkipRest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Exercise ${state.itemIndex + 1} of ${state.itemCount}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = state.item.exercise.name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Set ${state.setNumber} of ${state.item.sets}  ·  target ${state.item.loadLabel()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.isResting) {
            RestCountdown(
                remainingSeconds = state.restRemainingSeconds ?: 0,
                totalSeconds = state.item.restSeconds,
                onSkip = onSkipRest,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AllenaTextField(
                value = state.entry,
                onValueChange = onEntryChange,
                label = if (state.item.isTimed) "Seconds" else "Reps",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            if (!state.item.isTimed) {
                AllenaTextField(
                    value = state.weight,
                    onValueChange = onWeightChange,
                    label = "Kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AllenaButton(
            text = if (state.isLastSet) "Finish workout" else "Set done",
            onClick = onCompleteSet,
            loading = state.saving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RestCountdown(remainingSeconds: Int, totalSeconds: Int, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Rest ${formatRest(remainingSeconds)}",
            style = MaterialTheme.typography.headlineSmall,
        )
        LinearProgressIndicator(
            progress = { if (totalSeconds == 0) 0f else remainingSeconds.toFloat() / totalSeconds },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onSkip) { Text("Skip rest") }
    }
}

@Composable
private fun FinishedSummary(state: WorkoutPlayerUiState.Finished, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Workout complete", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "${state.setCount} sets · ${formatVolume(state.totalVolume)} of volume",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (state.pending) {
            Text(
                text = "Saved on this device. It will upload once you're back online.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        AllenaButton(
            text = "Done",
            onClick = onExit,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun DiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discard workout?") },
        text = { Text("The sets you have logged so far will be deleted.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Discard") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep going") } },
    )
}

private fun formatVolume(volume: Double): String =
    if (volume % 1.0 == 0.0) "${volume.toInt()} kg" else "$volume kg"

private val previewRunning = WorkoutPlayerUiState.Running(
    planName = "Full Body A",
    item = previewPlan.items[1],
    itemIndex = 1,
    itemCount = 4,
    setNumber = 2,
    entry = "8",
    weight = "60",
)

@Preview(showBackground = true)
@Composable
private fun WorkoutPlayerRunningPreview() {
    AllenaTheme {
        WorkoutPlayerContent(
            state = previewRunning,
            onEntryChange = {}, onWeightChange = {}, onCompleteSet = {}, onSkipRest = {},
            onFinishEarly = {}, onDiscard = {}, onRetry = {}, onExit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutPlayerRestingDarkPreview() {
    AllenaTheme {
        WorkoutPlayerContent(
            state = previewRunning.copy(restRemainingSeconds = 78),
            onEntryChange = {}, onWeightChange = {}, onCompleteSet = {}, onSkipRest = {},
            onFinishEarly = {}, onDiscard = {}, onRetry = {}, onExit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlayerTimedPreview() {
    AllenaTheme {
        WorkoutPlayerContent(
            state = previewRunning.copy(item = previewPlan.items[3], itemIndex = 3, setNumber = 3, entry = "45"),
            onEntryChange = {}, onWeightChange = {}, onCompleteSet = {}, onSkipRest = {},
            onFinishEarly = {}, onDiscard = {}, onRetry = {}, onExit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutPlayerFinishedPendingPreview() {
    AllenaTheme {
        WorkoutPlayerContent(
            state = WorkoutPlayerUiState.Finished(setCount = 14, totalVolume = 4820.0, pending = true),
            onEntryChange = {}, onWeightChange = {}, onCompleteSet = {}, onSkipRest = {},
            onFinishEarly = {}, onDiscard = {}, onRetry = {}, onExit = {},
        )
    }
}
