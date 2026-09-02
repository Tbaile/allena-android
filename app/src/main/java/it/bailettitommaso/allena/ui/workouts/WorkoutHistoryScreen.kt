package it.bailettitommaso.allena.ui.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import it.bailettitommaso.allena.domain.model.WorkoutSession
import it.bailettitommaso.allena.ui.theme.AllenaTheme
import it.bailettitommaso.allena.ui.workouts.components.VolumeChart
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SessionDateFormat = DateTimeFormatter.ofPattern("EEE d MMM")

@Composable
fun WorkoutHistoryScreen(
    onBack: () -> Unit,
    viewModel: WorkoutHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WorkoutHistoryContent(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutHistoryContent(state: WorkoutHistoryUiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.sessions.isNotEmpty()) {
                        SummaryRow(state.summary)
                    }
                    Text(
                        text = "Weekly volume",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    VolumeChart(bars = state.chart)
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            if (state.sessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No workouts logged yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(state.sessions, key = { it.localId }) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    SessionRow(session = session, volumeDelta = state.volumeDeltas[session.localId])
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(summary: ProgressSummary, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatTile("Sessions", summary.sessionCount.toString(), Modifier.weight(1f))
        StatTile("Trained", formatDuration(summary.timeTrained), Modifier.weight(1f))
        StatTile("Weeks", "${summary.activeWeeks}/${summary.trackedWeeks}", Modifier.weight(1f))
        StatTile("Volume", summary.volumeTrend?.let(::formatPercentDelta) ?: "—", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SessionRow(session: WorkoutSession, volumeDelta: Double?) {
    val trailing: (@Composable () -> Unit)? = when {
        session.isPending -> {
            { AssistChip(onClick = {}, label = { Text("Not uploaded") }) }
        }

        volumeDelta != null -> {
            {
                Text(
                    text = formatPercentDelta(volumeDelta),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (volumeDelta < 0.0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }

        else -> null
    }

    ListItem(
        overlineContent = { Text(session.startedAt.atZone(ZoneId.systemDefault()).format(SessionDateFormat)) },
        headlineContent = { Text(session.planName ?: "Workout") },
        supportingContent = { Text(session.summary()) },
        trailingContent = trailing,
    )
}

/**
 * Volume and time, not sets: a scheda prescribes a fixed number of sets and the player logs them
 * all, so a set count is the same number on every row.
 */
private fun WorkoutSession.summary(): String = buildList {
    if (totalVolume > 0.0) add(formatVolume(totalVolume))
    elapsed().takeIf { !it.isZero }?.let { add(formatDuration(it)) }
}.joinToString(" · ")

private fun previewSession(
    localId: Long,
    startedAt: String,
    setCount: Int,
    volume: Double,
    minutes: Long = 52,
    isPending: Boolean = false,
) = WorkoutSession(
    localId = localId,
    remoteId = if (isPending) null else localId,
    planId = 1,
    planName = "Full Body A",
    startedAt = Instant.parse(startedAt),
    completedAt = Instant.parse(startedAt).plus(Duration.ofMinutes(minutes)),
    notes = null,
    setCount = setCount,
    totalVolume = volume,
    isPending = isPending,
)

private val previewSessions = listOf(
    previewSession(3, "2026-08-26T18:00:00Z", 20, 5600.0, minutes = 48, isPending = true),
    previewSession(2, "2026-08-24T18:00:00Z", 20, 5200.0, minutes = 55),
    previewSession(1, "2026-08-19T18:00:00Z", 20, 4600.0, minutes = 51),
)

private val previewChart = weeklyVolume(previewSessions, today = LocalDate.of(2026, 8, 27))

private val previewState = WorkoutHistoryUiState(
    sessions = previewSessions,
    chart = previewChart,
    summary = progressSummary(previewSessions, previewChart),
    volumeDeltas = volumeDeltas(previewSessions),
)

@Preview(showBackground = true)
@Composable
private fun WorkoutHistoryContentPreview() {
    AllenaTheme {
        WorkoutHistoryContent(state = previewState, onBack = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WorkoutHistoryContentDarkPreview() {
    AllenaTheme {
        WorkoutHistoryContent(state = previewState, onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutHistoryContentEmptyPreview() {
    AllenaTheme {
        WorkoutHistoryContent(state = WorkoutHistoryUiState(), onBack = {})
    }
}
