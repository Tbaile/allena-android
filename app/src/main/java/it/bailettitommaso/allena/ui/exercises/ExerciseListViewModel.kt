package it.bailettitommaso.allena.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.domain.model.Category
import it.bailettitommaso.allena.domain.model.Exercise
import it.bailettitommaso.allena.domain.repository.CategoriesResult
import it.bailettitommaso.allena.domain.repository.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseFilter(val query: String = "", val categorySlug: String? = null)

data class ExercisesUiState(
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val selectedCategorySlug: String? = null,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisesUiState())
    val state: StateFlow<ExercisesUiState> = _state.asStateFlow()

    private val filter = MutableStateFlow(ExerciseFilter())
    private var debounceJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: Flow<PagingData<Exercise>> = filter
        .flatMapLatest { current ->
            exerciseRepository.list(search = current.query.ifBlank { null }, categorySlug = current.categorySlug)
        }
        .cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            filter.update { it.copy(query = value) }
        }
    }

    fun onCategorySelect(slug: String?) {
        if (_state.value.selectedCategorySlug == slug) return
        _state.update { it.copy(selectedCategorySlug = slug) }
        debounceJob?.cancel()
        filter.update { it.copy(categorySlug = slug) }
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
