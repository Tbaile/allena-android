package it.bailettitommaso.allena.ui.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
                    Text(
                        text = "Weekly volume",
                        style = MaterialTheme.typography.titleMedium,
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
                    SessionRow(session)
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: WorkoutSession) {
    ListItem(
        overlineContent = { Text(session.startedAt.atZone(ZoneId.systemDefault()).format(SessionDateFormat)) },
        headlineContent = { Text(session.planName ?: "Workout") },
        supportingContent = { Text(session.summary()) },
        trailingContent = if (session.isPending) {
            { AssistChip(onClick = {}, label = { Text("Not uploaded") }) }
        } else {
            null
        },
    )
}

private fun WorkoutSession.summary(): String {
    val sets = if (setCount == 1) "1 set" else "$setCount sets"
    if (totalVolume <= 0.0) return sets

    return "$sets · ${totalVolume.toInt()} kg"
}

private fun previewSession(
    localId: Long,
    startedAt: String,
    setCount: Int,
    volume: Double,
    isPending: Boolean = false,
) = WorkoutSession(
    localId = localId,
    remoteId = if (isPending) null else localId,
    planId = 1,
    planName = "Full Body A",
    startedAt = Instant.parse(startedAt),
    completedAt = Instant.parse(startedAt),
    notes = null,
    setCount = setCount,
    totalVolume = volume,
    isPending = isPending,
)

private val previewState = WorkoutHistoryUiState(
    sessions = listOf(
        previewSession(3, "2026-08-26T18:00:00Z", 14, 2400.0, isPending = true),
        previewSession(2, "2026-08-24T18:00:00Z", 20, 5200.0),
        previewSession(1, "2026-08-19T18:00:00Z", 18, 4600.0),
    ),
    chart = weeklyVolume(
        listOf(
            previewSession(2, "2026-08-24T18:00:00Z", 20, 5200.0),
            previewSession(1, "2026-08-17T18:00:00Z", 18, 4600.0),
            previewSession(0, "2026-08-03T18:00:00Z", 16, 3800.0),
        ),
        today = LocalDate.of(2026, 8, 27),
    ),
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
