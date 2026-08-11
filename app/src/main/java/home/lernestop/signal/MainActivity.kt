package home.lernestop.signal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import home.lernestop.signal.ui.navigation.AppNavigation
import home.lernestop.signal.ui.navigation.ExternalIntentViewModel
import home.lernestop.signal.ui.theme.SignalTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val externalIntentViewModel: ExternalIntentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedLink = intent.getStringExtra(Intent.EXTRA_TEXT)
        intent = Intent()
        externalIntentViewModel.onNewLinkReceived(sharedLink)

        enableEdgeToEdge()
        setContent {
            SignalTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val sharedLink = intent.getStringExtra(Intent.EXTRA_TEXT)
        setIntent(Intent())
        externalIntentViewModel.onNewLinkReceived(sharedLink)
    }
}