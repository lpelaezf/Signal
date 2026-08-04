package home.lernestop.signal.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExternalIntentViewModel : ViewModel() {
    private val _externalLink = MutableStateFlow<String?>(null)
    val externalLink = _externalLink.asStateFlow()

    fun onNewLinkReceived(link: String?) {
        _externalLink.value = link
    }

    fun consumeLink() {
        _externalLink.value = null
    }
}