package home.lernestop.signal.ui.screen.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import home.lernestop.signal.core.exception.SignalException
import home.lernestop.signal.data.repo.SignalRepo
import home.lernestop.signal.ui.mapper.toVideoUi
import home.lernestop.signal.ui.navigation.StartRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock

const val TAG_VIEW_MODEL = "MainViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SignalRepo,
) : ViewModel() {

    // --- 1. STATE ---

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val state get() = _uiState.value

    val externalLink = savedStateHandle.toRoute<StartRoute>().externalLink

    var ytLink by mutableStateOf("")
        private set

    // --- 2. EVENT HANDLER ---

    fun onEvent(event: HomeUiEvents) {
        when (event) {
            HomeUiEvents.OnAccept -> onAccept()
            HomeUiEvents.OnAdd -> onAdd()
            HomeUiEvents.OnCancel -> onCancel()
            HomeUiEvents.OnCheckedChange -> onCheckedChange()
            HomeUiEvents.OnDelete -> onDelete()
            HomeUiEvents.OnDismissDialog -> onDismissDialog()
            HomeUiEvents.OnDismissDialogError -> resetNetError()
            is HomeUiEvents.OnCardSelected -> onCardSelected(event.idItem)
            is HomeUiEvents.OnInputUpdate -> onInputUpdate(event.newValue)
            is HomeUiEvents.OnLongClick -> onLongClick(event.idItem)
        }
    }

    // --- 3. UI ACTIONS ---

    // -- Link Dialog --
    private fun onAdd() {
        showDialog()
    }

    private fun onInputUpdate(newValue: String) {
        if (state.textFieldError !is TextFieldErrors.None) {
            updateTextFieldErrors(TextFieldErrors.None)
        }
        updateLink(newValue.trim())
    }

    private fun onAccept() {
        checkLink()

        if (state.textFieldError is TextFieldErrors.None) {
            val videoId = extractVideoId()
            onDismissDialog()
            getVideoData(videoId)
        }
    }

    private fun onDismissDialog() {
        closeDialog()
        resetDialogRequestStates()
    }

    private fun showDialog() = updateShowDialog(answer = true)

    private fun closeDialog() = updateShowDialog(answer = false)

    // -- Selection Mode --
    private fun onLongClick(idItem: String) = activateSelectionMode(idItem)

    private fun activateSelectionMode(id: String) {
        updateCardSelection(id, selected = true)
        updateTopAppBarState()
        updateSelectionMode(true)
    }

    private fun onCardSelected(id: String) {
        toggleSelection(id)
        updateTopAppBarState()
    }

    private fun onCheckedChange() {
        when (state.checkBoxSelection) {
            CheckBoxState.NONE, CheckBoxState.INDETERMINATE -> selectAll()
            CheckBoxState.ALL -> resetSelectionModeStates()
        }
    }

    private fun onDelete() {
        deleteItemsSelected(state.cardsSelected)
        deactivateSelectionMode()
    }

    private fun onCancel() = deactivateSelectionMode()

    private fun deactivateSelectionMode() {
        resetSelectionModeStates()
        updateSelectionMode(false)
    }

    // -- Data Actions --
    fun getVideoData(videoId: String) {
        viewModelScope.launch {
            updateLoading(true)

            try {
                repo.findVideo(videoId)
                _uiState.update {
                    it.copy(navigateToResult = Navigation.ToSignalScreen(videoId))
                }
            } catch (e: SignalException) {
                handleConnectionError(e)
            }

            updateLoading(false)
        }
    }

    fun resetNavigationState() {
        _uiState.update { it.copy(navigateToResult = Navigation.Stay) }
    }

    // --- 4. PRIVATE LOGIC ---

    // -- Validations & Helpers --
    private fun checkLink() {
        if (ytLink.isBlank()) {
            updateTextFieldErrors(TextFieldErrors.Blank)
            Log.e(TAG_VIEW_MODEL, "The text field was empty")
            return
        }

        if (!isValidUrl()) {
            updateTextFieldErrors(TextFieldErrors.Url)
            Log.e(TAG_VIEW_MODEL, "The link was not a valid URL")
            return
        }

        if (extractVideoId().isEmpty()) {
            updateTextFieldErrors(TextFieldErrors.YoutubeLink)
            Log.e(TAG_VIEW_MODEL, "The link was not a YouTube video link")
            return
        }
    }

    private fun isValidUrl() = PatternsCompat.WEB_URL.matcher(ytLink).matches()

    /**
     * Parses the current [ytLink] and extracts the 11-character YouTube video ID.
     * Supports standard URLs, shorts, and live stream links from both youtube.com and youtu.be.
     * 
     * @return The extracted video ID or an empty string if not found.
     */
    private fun extractVideoId(): String {
        val uri = ytLink.toUri()
        val host = uri.host ?: ""

        return when {
            host.endsWith("youtube.com") -> {
                when {
                    uri.path?.startsWith("/watch") == true -> {
                        uri.getQueryParameter("v") ?: ""
                    }

                    (uri.path?.startsWith("/shorts") == true) ||
                            (uri.path?.startsWith("/live") == true) -> {
                        uri.pathSegments.find {
                            Regex("^[A-Za-z0-9_-]{11}$").matches(it)
                        } ?: ""
                    }

                    else -> ""
                }
            }

            host.endsWith("youtu.be") -> {
                uri.pathSegments.find {
                    Regex("^[A-Za-z0-9_-]{11}$").matches(it)
                } ?: ""
            }

            else -> ""
        }
    }

    // -- Error Handling --
    private fun handleConnectionError(e: SignalException) {
        val error = when (e) {
            is SignalException.RemoteServiceException -> NetErrors.Service
            is SignalException.VideoNotFoundException -> NetErrors.Video
            is SignalException.NetworkException -> NetErrors.Internet
            is SignalException.CommentException -> NetErrors.Comment
            is SignalException.GenerateInteractionException -> NetErrors.Interaction
            is SignalException.QuotaExceededException -> NetErrors.Quota(determinateNextQuotaReset())
            else -> NetErrors.None
        }
        updateNetErrors(error)
        Log.e(TAG_VIEW_MODEL, e.message, e)
    }

    private fun determinateNextQuotaReset(): String {
        val currentTimezone = TimeZone.currentSystemDefault()
        val usPacificTimeZone = TimeZone.of("America/Los_Angeles")

        val nextPacificDate = Clock.System.now()
            .toLocalDateTime(usPacificTimeZone)
            .date
            .plus(1, DateTimeUnit.DAY)

        val nextQuotaReset = LocalDateTime(nextPacificDate, LocalTime(0, 0))
            .toInstant(usPacificTimeZone)
            .toLocalDateTime(currentTimezone)

        return nextQuotaReset.toString()
    }

    private fun resetNetError() {
        updateNetErrors(NetErrors.None)
    }

    // -- State Updaters --
    private fun updateLink(link: String) {
        ytLink = link
    }

    private fun updateShowDialog(answer: Boolean) {
        _uiState.update { it.copy(showDialogRequestLink = answer) }
    }

    private fun updateLoading(value: Boolean) {
        _uiState.update { it.copy(isLoading = value) }
    }

    private fun updateNetErrors(error: NetErrors) {
        _uiState.update { it.copy(netErrors = error) }
    }

    private fun updateTextFieldErrors(error: TextFieldErrors) {
        _uiState.update { it.copy(textFieldError = error) }
    }

    private fun resetDialogRequestStates() {
        updateLink("")
        updateTextFieldErrors(TextFieldErrors.None)
    }

    private fun updateSelectionMode(selectionMode: Boolean) {
        _uiState.update { it.copy(selectionMode = selectionMode) }
    }

    private fun updateCardSelection(id: String, selected: Boolean) {
        _uiState.update {
            it.copy(
                cardsSelected = it.cardsSelected.toMutableList().apply {
                    if (selected) add(id)
                    else remove(id)
                }
            )
        }
    }

    private fun toggleSelection(id: String) {
        updateCardSelection(id, selected = !state.cardsSelected.contains(id))
    }

    private fun resetSelectionModeStates() {
        _uiState.update {
            it.copy(
                checkBoxSelection = CheckBoxState.NONE,
                selectedAmount = 0,
                cardsSelected = listOf()
            )
        }
    }

    private fun selectAll() {
        _uiState.update {
            it.copy(
                checkBoxSelection = CheckBoxState.ALL,
                selectedAmount = it.videosUi.size,
                cardsSelected = it.videosUi.map { video -> video.id }
            )
        }
    }

    private fun updateTopAppBarState() {
        val cardsCountSelected = state.cardsSelected.size
        val cardsTotal = state.videosUi.size

        updateCheckBoxSelection(cardsCountSelected, cardsTotal)
        updateTopAppBarSelectionModeTitle(cardsCountSelected)
    }

    private fun updateCheckBoxSelection(countSelected: Int, total: Int) {
        val checkBoxSelection = when (countSelected) {
            total -> CheckBoxState.ALL
            0 -> CheckBoxState.NONE
            else -> CheckBoxState.INDETERMINATE
        }

        _uiState.update { it.copy(checkBoxSelection = checkBoxSelection) }
    }

    private fun updateTopAppBarSelectionModeTitle(amount: Int) {
        _uiState.update { it.copy(selectedAmount = amount) }
    }

    // -- Data & Observations --
    private fun observeVideos() {
        viewModelScope.launch {
            repo.getVideos().collect { videos ->
                _uiState.update {
                    it.copy(
                        videosUi = videos.map { video -> video.toVideoUi() }
                    )
                }
            }
        }
    }

    private fun deleteItemsSelected(ids: List<String>) {
        viewModelScope.launch {
            repo.deleteVideos(ids)
        }
    }

    /**
     * Processes an external link received via navigation (e.g., from an Intent).
     * If a link exists, it pre-fills the dialog and attempts to fetch video data.
     */
    private fun handleExternalLinkReception() {
        externalLink?.let { link ->
            updateLink(link)
            showDialog()
            onAccept()
        }
    }

    // --- 5. INITIALIZATION ---

    init {
        observeVideos()
        handleExternalLinkReception()
    }
}

sealed class HomeUiEvents {
    data object OnAccept : HomeUiEvents()
    data object OnAdd : HomeUiEvents()
    data object OnCancel : HomeUiEvents()
    data object OnCheckedChange : HomeUiEvents()
    data object OnDelete : HomeUiEvents()
    data object OnDismissDialog : HomeUiEvents()
    data object OnDismissDialogError : HomeUiEvents()
    data class OnCardSelected(val idItem: String) : HomeUiEvents()
    data class OnInputUpdate(val newValue: String) : HomeUiEvents()
    data class OnLongClick(val idItem: String) : HomeUiEvents()
}
