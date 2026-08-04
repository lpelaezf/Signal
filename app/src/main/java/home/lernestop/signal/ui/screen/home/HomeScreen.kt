package home.lernestop.signal.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import home.lernestop.signal.R
import home.lernestop.signal.ui.model.VideoCard
import home.lernestop.signal.ui.theme.SignalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel, onItemClick: (String) -> Unit) {

    val mainUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mainUiState.navigateToResult) {
        val navigation = mainUiState.navigateToResult
        if (navigation is Navigation.ToSignalScreen) {
            onItemClick(navigation.videoId)
            homeViewModel.resetNavigationState()
        }
    }

    HomeScreenContent(
        uiState = mainUiState,
        linkValue = homeViewModel.ytLink,
        onEvent = homeViewModel::onEvent,
        onItemClick = onItemClick,
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    linkValue: String,
    onEvent: (HomeUiEvents) -> Unit,
    onItemClick: (String) -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (uiState.selectionMode) {
                SelectionModeTopAppBar(
                    selectedAmount = uiState.selectedAmount,
                    checkBoxState = uiState.checkBoxSelection,
                    onCheckedChange = { onEvent(HomeUiEvents.OnCheckedChange) },
                    onCancel = { onEvent(HomeUiEvents.OnCancel) }
                )
            }
            else {
                NormalModeTopAppBar(
                    onAdd = { onEvent(HomeUiEvents.OnAdd) }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            LazyColumn(modifier = Modifier
                .fillMaxSize()) {
                items(
                    items = uiState.videosUi,
                    key = {it.id}
                ) {item ->
                    if (uiState.selectionMode) {
                        CardItem(
                            videoCard = item,
                            isSelected = uiState.cardsSelected.contains(item.id),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.small_padding))
                                .clickable(
                                    onClick = {
                                        onEvent(HomeUiEvents.OnCardSelected(item.id))
                                    }
                                )
                        )
                    }
                    else {
                        CardItem(
                            videoCard = item,
                            isSelected = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.small_padding))
                                .combinedClickable(
                                    onClick = { onItemClick(item.id) },
                                    onLongClick = {
                                        onEvent(HomeUiEvents.OnLongClick(item.id))
                                    }
                                )
                        )
                    }
                }
            }

            if (uiState.selectionMode) {
                FloatingActionButton(
                    onClick = {onEvent(HomeUiEvents.OnDelete) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.medium_padding))
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete_button)
                    )
                }
            }
        }

        if (uiState.showDialogRequestLink) {
            DialogRequestLink(
                linkValue = linkValue,
                onDismiss = { onEvent(HomeUiEvents.OnDismissDialog) },
                onInputUpdate = { onEvent(HomeUiEvents.OnInputUpdate(it)) },
                textFieldErrors = uiState.textFieldError,
                onAccept = { onEvent(HomeUiEvents.OnAccept) }
            )
        }

        if (uiState.isLoading) {
            DialogLoading()
        }

        if (uiState.netErrors != NetErrors.None) {
            DialogErrorMessage(uiState.netErrors) {
                onEvent(HomeUiEvents.OnDismissDialogError)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalModeTopAppBar(onAdd: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.signals_found),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = onAdd) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_action_button)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeTopAppBar(
    selectedAmount: Int,
    checkBoxState: CheckBoxState,
    onCheckedChange: () -> Unit,
    onCancel: () -> Unit) {

    val triState = when(checkBoxState) {
        CheckBoxState.ALL -> ToggleableState.On
        CheckBoxState.NONE -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                TriStateCheckbox(
                    state = triState,
                    onClick = onCheckedChange,
                )

                Text(stringResource(R.string.selected_amount, selectedAmount))
            }
        },
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun CardItem(
    videoCard: VideoCard,
    isSelected: Boolean?,
    modifier: Modifier = Modifier) {

    Card(
        border = if (isSelected == true) {
            BorderStroke(
                width = dimensionResource(R.dimen.card_border),
                color = MaterialTheme.colorScheme.primary)
        } else null,
        modifier = modifier) {
        Row{
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(videoCard.thumbnail)
                    .crossfade(enable = true)
                    .build(),
                error = painterResource(R.drawable.image_error),
                placeholder = painterResource(R.drawable.place_holder),
                contentDescription = stringResource(R.string.video_thumbnail),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimensionResource(R.dimen.thumbnail_width))
            )

            DescriptionView(
                videoCard,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(R.dimen.small_padding),
                        top = dimensionResource(R.dimen.small_padding),
                        end = dimensionResource(R.dimen.tiny_padding),
                        bottom = dimensionResource(R.dimen.tiny_padding),
                    )
            )
        }
    }
}

@Composable
fun DescriptionView(
    videoCard: VideoCard,
    modifier: Modifier = Modifier) {

    Column (modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = videoCard.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = videoCard.creator,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
         StatisticsRow(
             views = videoCard.viewsCount,
             likes = videoCard.likesCount,
             commentsAmount = videoCard.commentsCount,
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(
                     top = dimensionResource(R.dimen.small_padding)
                 ))
    }
}

@Composable
fun StatisticsRow(
    views: String,
    likes: String,
    commentsAmount: String,
    modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ){
        Icon(
            painter = painterResource(R.drawable.ic_view),
            contentDescription = stringResource(R.string.view_icon),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.tiny_padding))
        )
        Text(
            text = views,
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.tiny_padding),
                    top = dimensionResource(R.dimen.tiny_padding)),
            style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.width(dimensionResource(R.dimen.spacer_between_statistics_icons)))

        Icon(
            painter = painterResource(R.drawable.ic_like),
            contentDescription = stringResource(R.string.like_icon)
        )
        Text(
            text = likes,
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.tiny_padding),
                    top = dimensionResource(R.dimen.tiny_padding)),
            style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.width(dimensionResource(R.dimen.spacer_between_statistics_icons)))

        Icon(
            painter = painterResource(R.drawable.ic_comment),
            contentDescription = stringResource(R.string.comments_icon),
            modifier = Modifier
                .padding(top = dimensionResource(R.dimen.tiny_padding))
        )
        Text(
            text = commentsAmount,
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.tiny_padding),
                    top = dimensionResource(R.dimen.tiny_padding)),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DialogRequestLink(
    linkValue: String,
    onDismiss: () -> Unit,
    onInputUpdate: (String) -> Unit,
    textFieldErrors: TextFieldErrors,
    onAccept: () -> Unit) {

    Dialog(onDismissRequest = onDismiss) {
        ContentRequestLinkDialog(
            newValue = linkValue,
            onDismiss = onDismiss,
            onValueChange = onInputUpdate,
            textFieldErrors = textFieldErrors,
            onAccept = onAccept,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun ContentRequestLinkDialog(
    newValue: String,
    onDismiss: () -> Unit,
    onValueChange: (String) -> Unit,
    textFieldErrors: TextFieldErrors,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier, ) {
    Card( modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.small_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.provide_the_link),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.tiny_padding))
            )

            OutlinedTextField(
                value = newValue,
                onValueChange = onValueChange,
                label = {Text(text = stringResource(R.string.provide_link_text_field))},
                singleLine = true,
                isError = textFieldErrors !is TextFieldErrors.None,
                supportingText = {
                    when(textFieldErrors) {
                        TextFieldErrors.Blank -> {
                            Text(text = stringResource(R.string.message_error_blank_text_field))
                        }

                        TextFieldErrors.YoutubeLink -> {
                            Text(text = stringResource(R.string.message_error_youtube_link))
                        }

                        TextFieldErrors.Url -> {
                            Text(text = stringResource(R.string.message_error_invalid_url))
                        }

                        TextFieldErrors.None -> {/* Nothing happens */}
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onAccept() }
                ),
                modifier = Modifier.padding(dimensionResource(R.dimen.xlarge_padding))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.dismiss))
                }

                TextButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .padding(end = dimensionResource(R.dimen.small_padding))) {
                    Text(text = stringResource(R.string.accept))
                }
            }

        }
    }
}

@Composable
fun ContentLoadingDialog() {
    Card {
        CircularProgressIndicator(
            modifier = Modifier.padding(dimensionResource(R.dimen.large_padding))
        )
    }
}

@Composable
fun DialogLoading() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )) {
        ContentLoadingDialog()
    }
}

@Composable
fun DialogErrorMessage(error: NetErrors, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        ContentDialogErrorMessage(
            error = error,
            onDismiss = onDismissRequest,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun ContentDialogErrorMessage(
    error: NetErrors,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier) {

    Card (modifier = modifier){
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.small_padding))
        ) {
            when(error) {
                NetErrors.Service -> {
                    TextDialogError(stringResource(R.string.message_error_service))
                }

                NetErrors.Internet -> {
                    TextDialogError(stringResource(R.string.message_error_no_internet))
                }

                NetErrors.Video -> {
                    TextDialogError(stringResource(R.string.message_error_no_video))
                }

                NetErrors.Comment -> {
                    TextDialogError(stringResource(R.string.message_error_no_comment))
                }

                NetErrors.Interaction -> {
                    TextDialogError(stringResource(R.string.message_error_signal_could_not_be_generated))
                }

                NetErrors.Quota -> {
                    TextDialogError(stringResource(R.string.message_error_quota))
                }

                NetErrors.None -> { /*No error*/}
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.padding(12.dp)) {
                    Text(text = stringResource(R.string.dismiss))
                }
            }
        }
    }
}

@Composable
fun TextDialogError(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(dimensionResource(R.dimen.small_padding))
    )
}

@Preview
@Composable
private fun CardItemPreview() {
    CardItem(
        VideoCard(
            id = "",
            thumbnail = "",
            title = "Title 0",
            creator = "Creator 0",
            viewsCount = "1.5 K",
            likesCount = "999",
            commentsCount = "45"
        ),
        isSelected = null,
        Modifier
            .fillMaxWidth()
            .height(90.dp))
}

@Preview
@Composable
private fun HomePreview() {
    val data = listOf(
        VideoCard("1", "Image 1", "Title 1", "Creator 1", "0", "0", "0"),
        VideoCard("2", "Image 2", "Title 2", "Creator 2", "0", "0", "0"),
        VideoCard("3", "Image 3", "Title 3", "Creator 3", "0", "0", "0"),
        VideoCard("4", "Image 4", "Title 4", "Creator 4", "0", "0", "0"),
        VideoCard("5", "Image 5", "Title 5", "Creator 5", "0", "0", "0"),
        VideoCard("6", "Image 6", "Title 6", "Creator 6", "0", "0", "0"),
        VideoCard("7", "Image 7", "Title 7", "Creator 7", "0", "0", "0"),
        VideoCard("8", "Image 8", "Title 8", "Creator 8", "0", "0", "0"),
        VideoCard("9", "Image 9", "Title 9", "Creator 9", "0", "0", "0"),
        VideoCard("10", "Image 10", "Title 10", "Creator 10", "0", "0", "0")
    )

    SignalTheme {
        HomeScreenContent(
            uiState = HomeUiState(videosUi = data),
            linkValue = "",
            onEvent = {},
            onItemClick = {}
        )
    }
}

@Preview
@Composable
private fun DialogPreview() {
    SignalTheme {
        ContentRequestLinkDialog(
            newValue = "https://www.youtube.com/watch?v=xxxxxxxxxxx",
            onDismiss = {},
            onValueChange = {},
            textFieldErrors = TextFieldErrors.None,
            onAccept = {},
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    SignalTheme {
        ContentLoadingDialog()
    }
}

@Preview
@Composable
private fun ContentDialogErrorMessagePreview() {
    SignalTheme {
        ContentDialogErrorMessage(
            error = NetErrors.Interaction,
            onDismiss = {},
            Modifier
                .fillMaxWidth()
                )
    }
}