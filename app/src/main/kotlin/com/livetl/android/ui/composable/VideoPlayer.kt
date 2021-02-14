package com.livetl.android.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.livetl.android.databinding.VideoPlayerBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoId: String?,
    isLive: Boolean?,
) {
    // TODO: dispose?
    var player = remember<YouTubePlayer?> { null }

    DisposableEffect(videoId) {
        if (videoId != null) {
            player?.loadVideo(videoId, 0F)
        }
        onDispose {}
    }

    AndroidViewBinding(bindingBlock = VideoPlayerBinding::inflate, modifier = modifier) {
        with (youtubePlayerView.getPlayerUiController()) {
            enableLiveVideoUi(isLive ?: false)
            showVideoTitle(false)
            showYouTubeButton(false)
        }

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                player = youTubePlayer
                videoId?.let { youTubePlayer.loadVideo(it, 0F) }
            }

//            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
//            }
//
//            override fun onStateChange(
//                youTubePlayer: YouTubePlayer,
//                state: PlayerConstants.PlayerState
//            ) {
//            }
        })
        }
    }
}