package it.bailettitommaso.allena.ui.workouts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.domain.model.WorkoutPlan
import it.bailettitommaso.allena.domain.model.WorkoutPlanItem
import it.bailettitommaso.allena.domain.repository.SessionUploadResult
import it.bailettitommaso.allena.domain.repository.WorkoutPlanResult
import it.bailettitommaso.allena.domain.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

sealed interface WorkoutPlayerUiState {
    data object Loading : WorkoutPlayerUiState

    data class Running(
        val planName: String,
        val item: WorkoutPlanItem,
        val itemIndex: Int,
        val itemCount: Int,
        val setNumber: Int,
        val entry: String,
        val weight: String,
        val restRemainingSeconds: Int? = null,
        val saving: Boolean = false,
    ) : WorkoutPlayerUiState {
        val isResting: Boolean get() = restRemainingSeconds != null
        val isLastSet: Boolean get() = itemIndex == itemCount - 1 && setNumber == item.sets
    }

    /** [pending] means the workout is safely stored but still owes the server an upload. */
    data class Finished(val setCount: Int, val totalVolume: Double, val pending: Boolean) : WorkoutPlayerUiState

    data object NotFound : WorkoutPlayerUiState
    data object Offline : WorkoutPlayerUiState
    data object Error : WorkoutPlayerUiState
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>("planId") ?: 0L

    private val _state = MutableStateFlow<WorkoutPlayerUiState>(WorkoutPlayerUiState.Loading)
    val state: StateFlow<WorkoutPlayerUiState> = _state.asStateFlow()

    private var sessionLocalId: Long? = null
    private var loggedSets = 0
    private var loggedVolume = 0.0
    private var restJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun onEntryChange(value: String) = updateRunning { it.copy(entry = value.filter(Char::isDigit)) }

    fun onWeightChange(value: String) = updateRunning {
        it.copy(weight = value.filter { char -> char.isDigit() || char == '.' })
    }

    fun skipRest() {
        restJob?.cancel()
        updateRunning { it.copy(restRemainingSeconds = null) }
    }

    /** Records the current set, then either rests before the next one or ends the workout. */
    fun completeSet() {
        val running = _state.value as? WorkoutPlayerUiState.Running ?: return
        val sessionId = sessionLocalId ?: return
        if (running.saving) return

        updateRunning { it.copy(saving = true) }
        viewModelScope.launch {
            val reps = if (running.item.isTimed) null else running.entry.toIntOrNull()
            val durationSeconds = if (running.item.isTimed) running.entry.toIntOrNull() else null
            val weight = running.weight.toDoubleOrNull()

            workoutRepository.logSet(
                sessionLocalId = sessionId,
                planItemId = running.item.id,
                setNumber = running.setNumber,
                reps = reps,
                weight = weight,
                durationSeconds = durationSeconds,
            )
            loggedSets++
            loggedVolume += (reps ?: 0) * (weight ?: 0.0)

            if (running.isLastSet) {
                finish()
            } else {
                advance(running)
            }
        }
    }

    /**
     * Moves past the current set without recording it, for a set the client could not perform. No
     * rest follows: there was no work to recover from.
     */
    fun skipSet() {
        val running = _state.value as? WorkoutPlayerUiState.Running ?: return
        if (running.saving || running.isLastSet) return

        restJob?.cancel()
        advance(running, rest = false)
    }

    /** Ends the workout early, keeping whatever was logged so far. */
    fun finishEarly() {
        if (_state.value !is WorkoutPlayerUiState.Running) return
        viewModelScope.launch { finish() }
    }

    /** Abandons the workout: the session row and its sets are thrown away. */
    fun discard() {
        restJob?.cancel()
        val sessionId = sessionLocalId ?: return
        sessionLocalId = null
        viewModelScope.launch { workoutRepository.discardSession(sessionId) }
    }

    private fun advance(running: WorkoutPlayerUiState.Running, rest: Boolean = true) {
        val movingToNextExercise = running.setNumber >= running.item.sets
        val nextIndex = if (movingToNextExercise) running.itemIndex + 1 else running.itemIndex
        val nextItem = plan?.items?.getOrNull(nextIndex) ?: return
        val nextSetNumber = if (movingToNextExercise) 1 else running.setNumber + 1

        _state.value = running.copy(
            item = nextItem,
            itemIndex = nextIndex,
            setNumber = nextSetNumber,
            // Within one exercise the athlete usually repeats what they just did, so their own
            // numbers carry over; a new exercise starts from what the plan prescribes.
            entry = if (movingToNextExercise) nextItem.defaultEntry() else running.entry,
            weight = if (movingToNextExercise) nextItem.defaultWeight() else running.weight,
            restRemainingSeconds = null,
            saving = false,
        )
        if (rest) startRest(running.item.restSeconds)
    }

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        if (seconds <= 0) return

        restJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                updateRunning { it.copy(restRemainingSeconds = remaining) }
                delay(SECOND_MILLIS)
            }
            updateRunning { it.copy(restRemainingSeconds = null) }
        }
    }

    private suspend fun finish() {
        restJob?.cancel()
        val sessionId = sessionLocalId ?: return

        val result = workoutRepository.finishSession(sessionId, Instant.now(), null)
        _state.value = WorkoutPlayerUiState.Finished(
            setCount = loggedSets,
            totalVolume = loggedVolume,
            pending = result == SessionUploadResult.Pending,
        )
        sessionLocalId = null
    }

    private var plan: WorkoutPlan? = null

    private fun load() {
        _state.value = WorkoutPlayerUiState.Loading
        viewModelScope.launch {
            when (val result = workoutRepository.plan(planId)) {
                is WorkoutPlanResult.Success -> start(result.plan)
                WorkoutPlanResult.NotFound -> _state.value = WorkoutPlayerUiState.NotFound
                WorkoutPlanResult.Offline -> _state.value = WorkoutPlayerUiState.Offline
                WorkoutPlanResult.Error -> _state.value = WorkoutPlayerUiState.Error
            }
        }
    }

    private suspend fun start(loaded: WorkoutPlan) {
        val first = loaded.items.firstOrNull()
        if (first == null) {
            Timber.d("workout plan %d has no exercises to run", loaded.id)
            _state.value = WorkoutPlayerUiState.NotFound
            return
        }

        plan = loaded
        sessionLocalId = workoutRepository.startSession(loaded.id, Instant.now())
        _state.value = WorkoutPlayerUiState.Running(
            planName = loaded.name,
            item = first,
            itemIndex = 0,
            itemCount = loaded.items.size,
            setNumber = 1,
            entry = first.defaultEntry(),
            weight = first.defaultWeight(),
        )
    }

    private fun updateRunning(transform: (WorkoutPlayerUiState.Running) -> WorkoutPlayerUiState.Running) {
        _state.update { current ->
            if (current is WorkoutPlayerUiState.Running) transform(current) else current
        }
    }

    private companion object {
        const val SECOND_MILLIS = 1_000L
    }
}

private fun WorkoutPlanItem.defaultEntry(): String =
    (if (isTimed) durationSeconds else reps)?.toString().orEmpty()

private fun WorkoutPlanItem.defaultWeight(): String =
    targetWeight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()
