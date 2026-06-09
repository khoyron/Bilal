package org.khoyron.bilal.ui.mosquefinder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MosqueFinderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MosqueFinderUiState())
    val uiState: StateFlow<MosqueFinderUiState> = _uiState
}

data class MosqueFinderUiState(
    val isLoading: Boolean = false
)