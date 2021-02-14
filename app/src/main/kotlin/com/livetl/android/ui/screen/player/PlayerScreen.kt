package com.livetl.android.ui.screen.player

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import com.livetl.android.R
import com.livetl.android.model.StreamInfo
import com.livetl.android.service.YouTubeVideoExtractor
import com.livetl.android.ui.composable.VideoPlayer
import com.livetl.android.ui.screen.player.tab.ChatTab
import com.livetl.android.ui.screen.player.tab.InfoTab
import com.livetl.android.ui.screen.player.tab.SettingsTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tabs(@StringRes val nameRes: Int) {
    Info(R.string.info),
    Chat(R.string.chat),
    Settings(R.string.settings),
}
val tabs = Tabs.values().toList()

@Composable
fun PlayerScreen(urlOrId: String) {
    val context = AmbientContext.current
    val coroutineScope = rememberCoroutineScope()

    var videoId by remember { mutableStateOf("") }
    var streamInfo by remember { mutableStateOf<StreamInfo?>(null) }

    var selectedTab by remember { mutableStateOf(Tabs.Info) }

    fun setSource(url: String) {
        videoId = YouTubeVideoExtractor.getVideoId(context, url)

        coroutineScope.launch {
            val newStream = YouTubeVideoExtractor.getStreamInfo(context, url)
            withContext(Dispatchers.Main) {
                streamInfo = newStream
            }
        }
    }

    DisposableEffect(urlOrId) {
        if (urlOrId.isNotEmpty()) {
            setSource(urlOrId)
        }
        onDispose {
            streamInfo = null
        }
    }

    Column {
        VideoPlayer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9F),
            videoId = videoId,
            isLive = streamInfo?.isLive
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    text = { Text(stringResource(tab.nameRes)) },
                    selected = index == selectedTab.ordinal,
                    onClick = { selectedTab = tab }
                )
            }
        }
        when (selectedTab) {
            Tabs.Info -> InfoTab(streamInfo = streamInfo)
            Tabs.Chat -> ChatTab(streamInfo = streamInfo)
            Tabs.Settings -> SettingsTab()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    LiveTLTheme {
//        PlayerScreen("")
//    }
//}