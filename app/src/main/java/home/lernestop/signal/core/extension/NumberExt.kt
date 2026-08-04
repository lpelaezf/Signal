package home.lernestop.signal.core.extension

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


/**
 * Formats counts compactly (e.g., 1.3M, 1M, 10K) while respecting local decimal separators and omitting zero decimals.
 */
fun Long.toFormattedCount(locale: Locale = Locale.getDefault()): String {
    return when {
        this >= 1_000_000_000 -> "${formatCompact(this / 1_000_000_000.0, locale)} B"
        this >= 1_000_000 -> "${formatCompact(this / 1_000_000.0, locale)} M"
        this >= 1_000 -> "${formatCompact(this / 1_000.0, locale)} K"
        else -> this.toString()
    }
}

/**
 * Private helper function to clean up trailing zeros in decimals.
 */
private fun formatCompact(value: Double, locale: Locale): String {
    val symbols = DecimalFormatSymbols(locale)
    val formatter = DecimalFormat("#.#", symbols)

    return formatter.format(value)
}