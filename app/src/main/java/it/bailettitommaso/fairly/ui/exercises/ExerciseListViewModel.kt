package it.bailettitommaso.fairly.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.fairly.domain.model.Category
import it.bailettitommaso.fairly.domain.model.Exercise
import it.bailettitommaso.fairly.domain.repository.CategoriesResult
import it.bailettitommaso.fairly.domain.repository.ExerciseRepository
import it.bailettitommaso.fairly.domain.repository.ExercisesResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExercisesError { OFFLINE, GENERIC }

data class ExercisesUiState(
    val isLoading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val selectedCategorySlug: String? = null,
    val error: ExercisesError? = null,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisesUiState())
    val state: StateFlow<ExercisesUiState> = _state.asStateFlow()

    private var reloadJob: Job? = null

    init {
        loadCategories()
        scheduleReload(debounce = false)
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        scheduleReload(debounce = true)
    }

    fun onCategorySelect(slug: String?) {
        if (_state.value.selectedCategorySlug == slug) return
        _state.update { it.copy(selectedCategorySlug = slug) }
        scheduleReload(debounce = false)
    }

    /**
     * Cancels any in-flight reload before launching a new one, so a slow older request can never
     * resolve after a newer one and overwrite fresher results (out-of-order response race).
     */
    private fun scheduleReload(debounce: Boolean) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            val current = _state.value
            _state.update { it.copy(isLoading = true, error = null) }
            val result = exerciseRepository.list(
                search = current.query.ifBlank { null },
                categorySlug = current.selectedCategorySlug,
            )
            when (result) {
                is ExercisesResult.Success ->
                    _state.update { it.copy(isLoading = false, exercises = result.exercises) }
                ExercisesResult.Offline ->
                    _state.update { it.copy(isLoading = false, error = ExercisesError.OFFLINE) }
                ExercisesResult.Error ->
                    _state.update { it.copy(isLoading = false, error = ExercisesError.GENERIC) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = exerciseRepository.categories()
            if (result is CategoriesResult.Success) {
                _state.update { it.copy(categories = result.categories) }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
