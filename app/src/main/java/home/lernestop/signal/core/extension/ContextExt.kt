package home.lernestop.signal.core.extension

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Shows a Toast message with the specified string resource and duration.
 */
fun Context.showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}