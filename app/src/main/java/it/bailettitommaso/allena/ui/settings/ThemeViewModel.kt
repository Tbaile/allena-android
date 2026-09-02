package it.bailettitommaso.allena.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bailettitommaso.allena.data.local.ThemeStore
import it.bailettitommaso.allena.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeStore: ThemeStore,
) : ViewModel() {

    /** Null until the stored choice has been read: lets the splash hold instead of flashing the wrong theme. */
    val mode: StateFlow<ThemeMode?> =
        themeStore.mode.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { themeStore.setMode(mode) }
    }
}
