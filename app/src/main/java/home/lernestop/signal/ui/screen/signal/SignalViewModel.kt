package home.lernestop.signal.ui.screen.signal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import home.lernestop.signal.data.local.projection.VideoSummaryWithSignal
import home.lernestop.signal.data.repo.SignalRepo
import home.lernestop.signal.ui.navigation.SignalRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SignalRepo,
): ViewModel() {

    // --- 1. STATE ---

    private val _uiState = MutableStateFlow(SignalUiState())
    val uiState = _uiState.asStateFlow()

    private val id = savedStateHandle.toRoute<SignalRoute>().id

    private fun loadData() {
        viewModelScope.launch {
            updateUi(videoSignal = repo.getSignal(id))
        }
    }

    private fun updateUi(videoSignal: VideoSummaryWithSignal) {
        _uiState.update {
            it.copy(
                topBarTitle = videoSignal.title,
                topBarSubtitle = videoSignal.creator,
                signal = videoSignal.signal,
            )
        }
    }

    init {
        loadData()
    }
}