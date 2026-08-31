package it.bailettitommaso.fairly.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.WorkoutSession
import it.bailettitommaso.fairly.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WorkoutHistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val chart: List<WeeklyVolume> = emptyList(),
)

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    /**
     * Read from the cache rather than the network so a workout that has not been uploaded yet
     * still shows up in the history and in the chart.
     */
    val state = workoutRepository.sessions()
        .map { sessions ->
            WorkoutHistoryUiState(
                sessions = sessions,
                chart = weeklyVolume(sessions, today = LocalDate.now()),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), WorkoutHistoryUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { workoutRepository.refreshSessions() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
