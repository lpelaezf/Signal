package home.lernestop.signal.ui.screen.signal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import home.lernestop.signal.R
import home.lernestop.signal.ui.theme.SignalTheme
import home.lernestop.signal.core.extension.showToast

const val TAG_SIGNAL_SCREEN = "SignalScreen"

@Composable
fun SignalScreen(signalViewModel: SignalViewModel, onNavigationUp: () -> Unit) {

    val signalUiState by signalViewModel.uiState.collectAsStateWithLifecycle()

    SignalScreenContent(
        uiState = signalUiState,
        onNavigationUp = onNavigationUp,
    )
}

@Composable
fun SignalScreenContent(
    uiState: SignalUiState,
    onNavigationUp: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val signal = uiState.signal ?: stringResource(R.string.message_error_no_signal_yet)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarSignal(
                title = uiState.topBarTitle,
                subtitle = uiState.topBarSubtitle,
                onNavigationUp = onNavigationUp,
                onShared = {
                    shareSignal(
                        context = context,
                        videoName = uiState.topBarTitle,
                        creator = uiState.topBarSubtitle,
                        signal = signal
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = signal,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.medium_padding))
                    .fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSignal(
    title: String,
    subtitle: String,
    onNavigationUp: () -> Unit,
    onShared: () -> Unit,
) {

    val horizontalScroll = rememberScrollState()

    CenterAlignedTopAppBar(
        title = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll)) {

                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigationUp) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.up_navigation_button)
                )
            }
        },
        actions = {
            IconButton(onClick = onShared) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.share_button)
                )
            }
        }
    )
}

private fun shareSignal(context: Context, videoName: String, creator: String, signal: String) {
    val signature = context.getString(R.string.signature)
    val message = "${videoName.uppercase()}\n\n${creator.uppercase()}\n\n$signal\n\n$signature"

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }

    val title = context.getString(R.string.share_this_signal_with)

    val shareIntent = Intent.createChooser(sendIntent, title)

    try {
        context.startActivity(shareIntent)
    } catch (e: ActivityNotFoundException) {
        handleAppNotFoundError(context, e)
    }
}

private fun handleAppNotFoundError(context: Context, e: ActivityNotFoundException) {
    Log.e(TAG_SIGNAL_SCREEN,"No sharing app was found", e)
    context.showToast(R.string.message_error_share_app_not_found)
}

@Preview(showBackground = true)
@Composable
private fun SignalScreePreview() {
    SignalTheme {
        SignalScreenContent(
            uiState = SignalUiState(
                signal = "This is a sample signal text to show how it looks in the preview.",
                topBarTitle = "Sample Title",
                topBarSubtitle = "Sample Creator"
            ),
            onNavigationUp = {}
        )
    }
}