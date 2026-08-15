package home.lernestop.signal.ui.screen.home

import home.lernestop.signal.ui.model.VideoCard

sealed class TextFieldErrors {
    data object None: TextFieldErrors()
    data object Blank: TextFieldErrors()
    data object YoutubeLink: TextFieldErrors()
    data object Url: TextFieldErrors()
}

sealed class NetErrors {
    data object None: NetErrors()
    data object Service: NetErrors()
    data object Internet: NetErrors()
    data object Video: NetErrors()
    data object Comment: NetErrors()
    data object Interaction: NetErrors()
    data class Quota(val nextReset: String): NetErrors()
}

sealed class Navigation {
    data object Stay: Navigation()
    data class ToSignalScreen(val videoId: String): Navigation()
}

enum class CheckBoxState{ALL, NONE, INDETERMINATE}

data class HomeUiState(
    val videosUi: List<VideoCard> = listOf(),
    val isLoading: Boolean = false,
    val showDialogRequestLink: Boolean = false,
    val textFieldError: TextFieldErrors = TextFieldErrors.None,
    val netErrors: NetErrors = NetErrors.None,
    val selectionMode: Boolean = false,
    val checkBoxSelection: CheckBoxState = CheckBoxState.NONE,
    val selectedAmount: Int = 0,
    val cardsSelected: List<String> = listOf(),
    val navigateToResult: Navigation = Navigation.Stay,
)