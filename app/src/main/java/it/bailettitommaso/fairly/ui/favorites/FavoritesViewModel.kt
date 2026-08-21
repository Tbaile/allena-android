package it.bailettitommaso.fairly.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    val favorites: StateFlow<List<Exercise>> = exerciseRepository.favorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch { exerciseRepository.toggleFavorite(exercise) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5000L
    }
}
