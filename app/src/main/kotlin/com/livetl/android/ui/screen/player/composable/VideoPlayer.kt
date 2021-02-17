package com.livetl.android.ui.screen.player.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.livetl.android.databinding.VideoPlayerBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoId: String?,
    isLive: Boolean?,
    onCurrentSecond: (Float) -> Unit,
    onStateChange: (PlayerState) -> Unit,
) {
    // TODO: dispose?
    var player = remember<YouTubePlayer?> { null }

    DisposableEffect(videoId) {
        if (!videoId.isNullOrBlank()) {
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

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onCurrentSecond(second)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                onStateChange(PlayerState.fromPlayerState(state))
            }
        })
    }
}

enum class PlayerState {
    UNKNOWN, ENDED, PLAYING, PAUSED, BUFFERING;

    companion object {
        fun fromPlayerState(state: PlayerConstants.PlayerState): PlayerState {
            return when (state) {
                PlayerConstants.PlayerState.ENDED -> ENDED
                PlayerConstants.PlayerState.PLAYING -> PLAYING
                PlayerConstants.PlayerState.PAUSED -> PAUSED
                PlayerConstants.PlayerState.BUFFERING -> BUFFERING
                else -> UNKNOWN
            }
        }
    }
}