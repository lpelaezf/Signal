package home.lernestop.signal.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class StartRoute(val externalLink: String?)

@Serializable
data class SignalRoute(val id: String)