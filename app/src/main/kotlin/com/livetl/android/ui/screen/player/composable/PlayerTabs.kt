package com.livetl.android.ui.screen.player.composable

import android.app.PictureInPictureParams
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livetl.android.R
import com.livetl.android.data.chat.ChatMessage
import com.livetl.android.data.stream.StreamInfo
import com.livetl.android.ui.screen.player.PlayerViewModel
import com.livetl.android.ui.screen.player.composable.chat.ChatState
import com.livetl.android.util.collectAsState
import com.livetl.android.util.findActivity
import com.livetl.android.util.rememberIsInPipMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

private enum class Tabs(@StringRes val nameRes: Int, val icon: ImageVector) {
    Chat(R.string.chat, Icons.AutoMirrored.Outlined.Chat),
    Settings(R.string.settings, Icons.Outlined.Settings),
}
private val tabs = Tabs.entries

@Composable
fun PlayerTabs(
    streamInfo: StreamInfo?,
    chatState: ChatState,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = viewModel(),
) {
    val isInPipMode = rememberIsInPipMode()

    val tlScale by viewModel.prefs.tlScale().collectAsState()
    val filteredMessages by viewModel.filteredMessages.collectAsState(initial = persistentListOf())

    if (isInPipMode) {
        ChatTab(
            filteredMessages = filteredMessages,
            fontScale = tlScale,
            state = chatState,
        )
        return
    }

    FullPlayerTab(
        streamInfo,
        filteredMessages,
        tlScale,
        chatState,
        modifier,
    )
}

@Composable
private fun FullPlayerTab(
    streamInfo: StreamInfo?,
    filteredMessages: ImmutableList<ChatMessage>,
    tlScale: Float,
    chatState: ChatState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current.findActivity()

    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(Tabs.Chat),
        pageCount = { tabs.size },
    )

    var showStreamInfo by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        if (showStreamInfo) {
            StreamInfoPanel(
                streamInfo = streamInfo,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = {},
                modifier = Modifier.weight(1f),
            ) {
                tabs.fastForEachIndexed { index, tab ->
                    Tab(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.nameRes),
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }

            Button(onClick = { showStreamInfo = !showStreamInfo }) {
                Icon(
                    imageVector = if (showStreamInfo) {
                        Icons.Outlined.KeyboardArrowUp
                    } else {
                        Icons.Outlined.KeyboardArrowDown
                    },
                    contentDescription = null,
                )
            }

            Button(
                onClick = {
                    activity.enterPictureInPictureMode(
                        PictureInPictureParams.Builder().build(),
                    )
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.PictureInPictureAlt,
                    contentDescription = null,
                )
            }
        }
        HorizontalDivider()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (page) {
                Tabs.Chat.ordinal ->
                    ChatTab(
                        modifier = Modifier.fillMaxSize(),
                        filteredMessages = filteredMessages,
                        fontScale = tlScale,
                        state = chatState,
                    )

                Tabs.Settings.ordinal -> SettingsTab()
            }
        }
    }
}
