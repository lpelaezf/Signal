package home.lernestop.signal.navigation

import kotlinx.serialization.Serializable

@Serializable
data class StartRoute(val externalLink: String?)

@Serializable
data class SignalRoute(val id: String)