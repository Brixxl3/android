package com.livetl.android.ui.screen.player.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.livetl.android.databinding.VideoPlayerBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoId: String?,
    isLive: Boolean?,
    onCurrentSecond: (Float) -> Unit,
) {
    var playerView: YouTubePlayerView? = null
    var player = remember<YouTubePlayer?> { null }

    DisposableEffect(videoId) {
        if (!videoId.isNullOrBlank()) {
            player?.loadVideo(videoId, 0F)
        }

        // TODO: do this properly
        onDispose {
//            playerView?.release()
        }
    }

    AndroidViewBinding(factory = VideoPlayerBinding::inflate, modifier = modifier) {
        playerView = youtubePlayerView

        with (youtubePlayerView.getPlayerUiController()) {
            enableLiveVideoUi(isLive ?: false)
            showVideoTitle(false)
            showYouTubeButton(false)
            showFullscreenButton(false)
        }

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                player = youTubePlayer
                videoId?.let { youTubePlayer.loadVideo(it, 0F) }
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onCurrentSecond(second)
            }
        })
    }
}