package it.bailettitommaso.fairly.ui.workouts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.repository.WorkoutPlanResult
import it.bailettitommaso.fairly.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WorkoutPlanDetailUiState {
    data object Loading : WorkoutPlanDetailUiState
    data class Success(val plan: WorkoutPlan) : WorkoutPlanDetailUiState
    data object NotFound : WorkoutPlanDetailUiState
    data object Offline : WorkoutPlanDetailUiState
    data object Error : WorkoutPlanDetailUiState
}

@HiltViewModel
class WorkoutPlanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _state = MutableStateFlow<WorkoutPlanDetailUiState>(WorkoutPlanDetailUiState.Loading)
    val state: StateFlow<WorkoutPlanDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = WorkoutPlanDetailUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = workoutRepository.plan(planId)) {
                is WorkoutPlanResult.Success -> WorkoutPlanDetailUiState.Success(result.plan)
                WorkoutPlanResult.NotFound -> WorkoutPlanDetailUiState.NotFound
                WorkoutPlanResult.Offline -> WorkoutPlanDetailUiState.Offline
                WorkoutPlanResult.Error -> WorkoutPlanDetailUiState.Error
            }
        }
    }
}
