package it.bailettitommaso.fairly.ui.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.repository.ExerciseRepository
import it.bailettitommaso.fairly.domain.repository.ExerciseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExerciseDetailUiState {
    data object Loading : ExerciseDetailUiState
    data class Success(val exercise: Exercise) : ExerciseDetailUiState
    data object NotFound : ExerciseDetailUiState
    data object Offline : ExerciseDetailUiState
    data object Error : ExerciseDetailUiState
}

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _state = MutableStateFlow<ExerciseDetailUiState>(ExerciseDetailUiState.Loading)
    val state: StateFlow<ExerciseDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun toggleFavorite() {
        val current = (_state.value as? ExerciseDetailUiState.Success)?.exercise ?: return
        viewModelScope.launch {
            exerciseRepository.toggleFavorite(current)
            _state.value = ExerciseDetailUiState.Success(current.copy(isFavorite = !current.isFavorite))
        }
    }

    private fun load() {
        _state.value = ExerciseDetailUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = exerciseRepository.get(exerciseId)) {
                is ExerciseResult.Success -> ExerciseDetailUiState.Success(result.exercise)
                ExerciseResult.NotFound -> ExerciseDetailUiState.NotFound
                ExerciseResult.Offline -> ExerciseDetailUiState.Offline
                ExerciseResult.Error -> ExerciseDetailUiState.Error
            }
        }
    }
}
