package it.bailettitommaso.fairly.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.WorkoutPlan
import it.bailettitommaso.fairly.domain.repository.WorkoutPlansResult
import it.bailettitommaso.fairly.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WorkoutPlansUiState {
    data object Loading : WorkoutPlansUiState
    data class Success(val plans: List<WorkoutPlan>) : WorkoutPlansUiState
    data object Offline : WorkoutPlansUiState
    data object Error : WorkoutPlansUiState
}

@HiltViewModel
class WorkoutPlansViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<WorkoutPlansUiState>(WorkoutPlansUiState.Loading)
    val state: StateFlow<WorkoutPlansUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = WorkoutPlansUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = workoutRepository.plans()) {
                is WorkoutPlansResult.Success -> WorkoutPlansUiState.Success(result.plans)
                WorkoutPlansResult.Offline -> WorkoutPlansUiState.Offline
                WorkoutPlansResult.Error -> WorkoutPlansUiState.Error
            }
        }
    }
}
